(ns md-specialista-import.services.database-gruppi
  (:require
    [md-specialista-import.configuration :as conf]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]))

(def db-specialista (:md-specialista-db conf/configuration))
(def db-summa (:summa-db conf/configuration))

(defn clean-gruppi! [asl-id]
  (do
    (jdbc/execute!
       db-specialista
       ["DELETE FROM gruppo_medico WHERE gruppomedico_gruppo_id IN (SELECT gruppo_id FROM gruppo WHERE gruppo_asl_id = ?)" asl-id])
    (jdbc/execute!
       db-specialista
       ["DELETE FROM gruppo_responsabile WHERE grupporesponsabile_gruppo_id IN (SELECT gruppo_id FROM gruppo WHERE gruppo_asl_id = ?)" asl-id])
    (jdbc/execute!
       db-specialista
       ["DELETE FROM gruppo WHERE  gruppo_asl_id = ?" asl-id])))

(defn get-dipartimenti []
  (jdbc/query db-summa "SELECT DISTINCT asl, dip_cod, dip_des AS des FROM vista_medici_specialisti WHERE dip_cod != ''"))

(defn get-aree []
  (jdbc/query db-summa "SELECT DISTINCT asl, dip_cod, area_cod, area_des AS des FROM vista_medici_specialisti WHERE area_cod != ''"))

(defn get-soc []
  (jdbc/query db-summa "SELECT DISTINCT asl, dip_cod, area_cod, soc_cod, soc_des AS des FROM vista_medici_specialisti WHERE soc_cod != ''"))

(defn get-sos []
  (jdbc/query db-summa "SELECT DISTINCT asl, dip_cod, area_cod, soc_cod, sos_cod, sos_des AS des FROM vista_medici_specialisti WHERE sos_cod != ''"))

(defn ->gruppo [asl-id rs]
  (let [{asl :asl dip_cod :dip_cod area_cod :area_cod soc_cod :soc_cod sos_cod :sos_cod des :des} rs]
    {:gruppo_descrizione (str/upper-case des)
     :gruppo_codice (str asl dip_cod area_cod soc_cod sos_cod)
     :gruppo_asl_id asl-id}))

(defn insert-gruppi! [gruppi]
  (jdbc/insert-multi! db-specialista :gruppo gruppi))

(defn insert-dipartimenti [asl-id]
  (->> (get-dipartimenti)
       (map (partial ->gruppo asl-id))
       (insert-gruppi!)))

(defn insert-aree [asl-id]
  (->> (get-aree)
       (map (partial ->gruppo asl-id))
       (insert-gruppi!)))

(defn insert-soc [asl-id]
  (->> (get-soc)
      (map (partial ->gruppo asl-id)
       (insert-gruppi!))))

(defn insert-sos [asl-id]
  (->> (get-sos)
       (map (partial ->gruppo asl-id))
       (insert-gruppi!)))


(defn rigenera-gruppi [asl-id]
  (do
    (clean-gruppi! [asl-id])
    (insert-dipartimenti [asl-id])
    (insert-aree [asl-id])
    (insert-soc [asl-id])
    (insert-sos [asl-id])))
