(ns md-specialista-import.services.database-asl
  (:require
    [md-specialista-import.configuration :as conf]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]))

(def db-specialista (:md-specialista-db conf/configuration))

(defn get-asl [regione codice]
  (jdbc/query db-specialista ["SELECT asl_id FROM asl WHERE asl_codice = ?"  (str regione codice)]))

(defn get-farmacisti [asl_id]
  (jdbc/query db-specialista ["SELECT * FROM utente WHERE utente_ruolo = 2 AND utente_asl = ?" asl_id]))
