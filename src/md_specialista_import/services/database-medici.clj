(ns md-specialista-import.services.database-medici
  (:require
    [md-specialista-import.configuration :as conf]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [clj-time.core :as time]
    [clj-time.coerce :as time-coerce]))

(def db-specialista (:md-specialista-db conf/configuration))
(def db-summa (:summa-db conf/configuration))

(def query-specialisti
  "SELECT fiscale, cognome,nome, sesso, email, asl, dip_cod, area_cod, soc_cod, sos_cod
  FROM vista_medici_specialisti
  WHERE fiscale != '' and dip_cod != '' and area_cod != '' and soc_cod != '' and sos_cod != ''
  ORDER BY FISCALE")

(defn get-specialisti []
  (jdbc/query db-summa query-specialisti))

(defn utente-exists? [rs]
  (first (jdbc/query db-specialista ["SELECT * FROM utente WHERE utente_codfiscale = ?" (:fiscale rs)])))

(defn medico-exists? [rs]
  (first (jdbc/query db-specialista ["SELECT * FROM medico WHERE medico_codfiscale = ?" (:fiscale rs)])))

(defn utente-not-exists? [rs]
  (not (utente-exists? rs)))

(defn medico-not-exists? [rs]
  (not (medico-exists? rs)))

(defn create-pwd [cognome]
  (let [base (if (< (count cognome) 5)
                (subs cognome 0 (count cognome))
                (subs cognome 0 5))
        base-formatted (str/replace (format "%-5s" (str/capitalize base)) " " "0")
        special-char (rand-nth ["@" "#" "$" "%" "&" "+" "="])
        number (format "%03d" (rand-int 1000))]
    (str base-formatted number special-char)))

(defn ->utente [asl-id rs]
    {:utente_mail  (str/trim (:email rs))
     :utente_password (create-pwd (:cognome rs))
     :utente_asl asl-id
     :utente_cognome (str/trim (str/upper-case (str (:cognome rs) " " (:nome rs))))
     :utente_nome ""
     :utente_dataattivazione  (time-coerce/to-sql-date (time/now))
     :utente_datascadenza (time-coerce/to-sql-date (time/plus (time/now) (time/days 365)))
     :utente_attivo   false
     :utente_abilitato true
     :utente_pin  (+ (rand-int 9000) 1000)
     :utente_ruolo 10
     :utente_codfiscale (str/upper-case (str/trim (:fiscale rs)))
     :utente_domanda "TUO CODICE FISCALE?"
     :utente_risposta (str/trim (:fiscale rs))})

(defn insert-utenti! [utenti]
  (jdbc/insert-multi! db-specialista :utente utenti))

(defn specialisti->utenti [asl-id]
  (->> (get-specialisti)
       (filter #(utente-not-exists? %))
       (map (partial ->utente asl-id))
       (insert-utenti!)))

(defn ->medico [asl-id rs]
  {:medico_codfiscale (str/upper-case (str/trim (:fiscale rs)))
   :medico_assistiti_f 0
   :medico_assistiti_m 0
   :medico_assistiti_tot 0
   :medico_asl asl-id
   :medico_sesso (str/upper-case (str/trim (:sesso rs)))
   :medico_ass_etamedia 0
   :medico_zona_cod nil
   :medico_zona_des nil
   :medico_fascia_cod nil
   :medico_fascia_des nil
   :medico_nominativo (str/trim (str/upper-case (str (:cognome rs) " " (:nome rs))))
   :medico_tipo 2
   :medico_presidio (Integer/parseInt (:asl rs))})

(defn insert-medici! [medici]
  (jdbc/insert-multi! db-specialista :medico medici))

(defn specialisti->medici [asl-id]
  (->> (get-specialisti)
       (filter #(medico-not-exists? %))
       (map (partial ->medico asl-id))
       (insert-medici!)))
