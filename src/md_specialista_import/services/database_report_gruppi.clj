(ns md-specialista-import.services.database-report-gruppi
  (:require
    [md-specialista-import.configuration :as conf]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [clj-time.core :as time]
    [clj-time.coerce :as time-coerce]
    [digest :as digest]
    [clj-time.core :as time]
    [clj-time.coerce :as time-coerce]
    [parallel.core :as p]
    [hikari-cp.core :as cp]))

(def db-specialista (:md-specialista-db conf/configuration))
(def db-report (:report-db conf/configuration))

(defonce db-report-par
  (delay (cp/make-datasource (:report-db-par conf/configuration))))
(defonce db-specialista-par
  (delay (cp/make-datasource (:specialista-db-par conf/configuration))))

(defn get-dipartimenti-referenti [m]
    (jdbc/query db-report ["SELECT DISTINCT asl_cod+dip_cod AS gruppo_codice, dip_ref AS gruppo_referente_cf, 'DIP' AS descrizione FROM report
                            WHERE regione=? AND asl=? AND anno=? AND mese_da=? AND mese_a=? AND report_code='rv22m1'
                            AND dip_ref is not null AND LEN(asl_cod+dip_cod)=5"
                            (:regione m) (:asl m) (:anno m) (:mese_da m) (:mese_a m)]))

(defn get-aree-referenti [m]
    (jdbc/query db-report ["SELECT DISTINCT asl_cod+dip_cod+area_cod AS gruppo_codice, area_ref AS gruppo_referente_cf, 'AREA' AS descrizione FROM report
                            WHERE regione=? AND asl=? AND anno=? AND mese_da=? AND mese_a=? AND report_code='rv22m1'
                            AND area_ref is not null AND LEN(asl_cod+dip_cod+area_cod)=8"
                            (:regione m) (:asl m) (:anno m) (:mese_da m) (:mese_a m)]))

(defn get-soc-referenti [m]
  (let [conn {:datasource @db-report-par}]
    (jdbc/query conn ["SELECT DISTINCT asl_cod+dip_cod+area_cod+soc_cod AS gruppo_codice, soc_ref AS gruppo_referente_cf, 'SOC' AS descrizione FROM report
                       WHERE regione=? AND asl=? AND anno=? AND mese_da=? AND mese_a=? AND report_code='rv22m1'
                       AND soc_ref is not null AND LEN(asl_cod+dip_cod+area_cod+soc_cod)=12"
                       (:regione m) (:asl m) (:anno m) (:mese_da m) (:mese_a m)])))

(defn get-sos-referenti [m]
  (jdbc/query db-report ["SELECT DISTINCT asl_cod+dip_cod+area_cod+soc_cod+sos_cod AS gruppo_codice, sos_ref AS gruppo_referente_cf, 'SOS' AS descrizione FROM report
                          WHERE regione=? AND asl=? AND anno=? AND mese_da=? AND mese_a=? AND report_code='rv22m1'
                          AND sos_ref is not null AND LEN(asl_cod+dip_cod+area_cod+soc_cod+sos_cod)=17"
                          (:regione m) (:asl m) (:anno m) (:mese_da m) (:mese_a m)]))

(defn medico-cf->medico-id [codfis]
  (let [conn {:datasource @db-specialista-par}]
    (jdbc/query db-specialista ["SELECT medico_id FROM medico WHERE medico_codfiscale=?" codfis])))

(defn get-report-medico [m]
    (let [conn {:datasource @db-report-par}]
       (jdbc/query conn
                    ["SELECT pdfData FROM report WHERE fiscale = ? AND regione = ? AND asl = ? AND anno = ? AND mese_da = ? AND mese_a = ? AND report_code ='rv22m1'"
                     (:codice-fiscale m) (:regione m) (:asl m) (:anno m) (:mese_da m) (:mese_a m)])))

(defn medico->gruppo-report [par rs]
   (let [{anno :anno mese_da :mese_da mese_a :mese_a} par
         report (get-report-medico (assoc par :codice-fiscale (:gruppo_codice rs)))
         medico-id (:medico_id (first (medico-cf->medico-id (:gruppo_referente_cf rs))))]
     {:report_data (time-coerce/to-sql-date (time/now))
      :report_medico_id medico-id
      :report_tipo 4
      :report_anno anno
      :report_periodo 4
      :report_descrizione (str (:descrizione rs) "_" mese_da anno "_" mese_a anno)
      :report_mimetype "application/pdf"
      :report_hash (if (empty? report)  nil (digest/sha-256 (:pdfdata (first report))))
      :report_document (if (empty? report)  nil (:pdfdata (first report)))
      :report_path "-"}))

(defn is-report? [rs]
  (and
   (some? (:report_document rs))
   (some? (:report_medico_id rs))))

(defn insert-report! [reports]
  (jdbc/insert-multi! db-specialista :report reports))

(defn import-dipartimenti-report [m]
  (->> m
       (get-dipartimenti-referenti)
       (filter #(not-empty(:gruppo_referente_cf %)))
       (pmap (partial  medico->gruppo-report m))
       (filter #(is-report? %))
       ;;(insert-report!)
       (count)))

(defn import-aree-report [m]
  (->> m
       (get-aree-referenti)
       (filter #(not-empty(:gruppo_referente_cf %)))
       (pmap (partial  medico->gruppo-report m))
       (filter #(is-report? %))
       ;;(insert-report!)
       (count)))

(defn import-soc-report [m]
  (->> m
       (get-soc-referenti)
       (filter #(not-empty(:gruppo_referente_cf %)))
       (pmap (partial  medico->gruppo-report m))
       (filter #(is-report? %))
       ;;(insert-report!)
       (count)))

(defn import-sos-report [m]
  (->> m
       (get-sos-referenti)
       (filter #(not-empty(:gruppo_referente_cf %)))
       (pmap (partial  medico->gruppo-report m))
       (filter #(is-report? %))
       ;;(insert-report!)
       (count)))
