(ns md-specialista-import.services.database-report
  (:require
    [md-specialista-import.configuration :as conf]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [clj-time.core :as time]
    [clj-time.coerce :as time-coerce]
    [digest :as digest]
    [clj-time.core :as time]
    [clj-time.coerce :as time-coerce]))

(def db-specialista (:md-specialista-db conf/configuration))
(def db-report (:report-db conf/configuration))

(defn cont-report []
  (jdbc/query db-report "SELECT COUNT(*) AS tot FROM report"))

(defn get-report-medico [m]
  (jdbc/query db-report
               ["SELECT pdfData FROM report WHERE fiscale = ? AND regione = ? AND asl = ? AND anno = ? AND mese_da = ? AND mese_a = ?"
                (:codice-fiscale m) (:regione m) (:asl m) (:anno m) (:mese_da m) (:mese_a m)]))

;;AND medico_id not in quelli che hanno già questi report
(defn get-medici [asl-id]
  (jdbc/query db-specialista ["SELECT * FROM medico WHERE medico_asl=?" asl-id]))

(defn medico->report [par rs]
   (let [{anno :anno mese_da :mese_da mese_a :mese_a} par
         report (get-report-medico (assoc par :codice-fiscale (:medico_codfiscale rs)))]
     {:report_data (time-coerce/to-sql-date (time/now))
      :report_medico_id (:medico_id rs)
      :report_tipo 4
      :report_anno anno
      :report_periodo 4
      :report_descrizione (str mese_da anno "_" mese_a anno)
      :report_mimetype "application/pdf"
      :report_hash (if (empty? report)  nil (digest/sha-256 (:pdfdata (first report))))
      :report_document (if (empty? report)  nil (:pdfdata (first report)))
      :report_path "-"}))

(defn is-report? [rs]
  (some? (:report_document rs)))

(defn insert-report! [reports]
  (jdbc/insert-multi! db-specialista :report reports))

(defn import-report [m]
  (->> (get-medici (:asl_id m))
       (map (partial medico->report m))
       (filter #(is-report? %))
       (insert-report!)
       (count)))
;;---------------
