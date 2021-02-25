(ns md-specialista-import.core
  (:require [clojure.tools.cli :as cli]
            [md-specialista-import.services.database-asl :as db-asl]
            [md-specialista-import.services.database-gruppi :as db-gruppi]
            [md-specialista-import.services.database-report :as db-report]
            [cambium.core :as log])
  (:gen-class))

(def cli-options
  [["-y" "--anno ANNO"
    :parse-fn #(Integer/parseInt %)]
   ["-b" "--mese_da MESE_DA"]
    ;;:parse-fn #(Integer/parseInt %)
    ;;:validate [#(< 1 (Integer/parseInt %) 12) "Mese partenza compreso tra 1 e 12"]]
   ["-e" "--mese_a MESE_A"]
    ;;:parse-fn #(Integer/parseInt %)
    ;;:validate [#(< 1 (Integer/parseInt %) 12) "Mese arrivo compreso tra 1 e 12"]]
   ["-r" "--regione REGIONE"]
   ["-a" "--asl ASL"]])


(defn import-report [m]
  (do
    ;;(db-gruppi/rigenera-gruppi)
    (db-report/import-report m)))
    ;;(println m)))

(defn -main
  [& args]
  (let [cli-res (cli/parse-opts args cli-options)
        cli-props (:options cli-res)
        asl (db-asl/get-asl (:regione cli-props) (:asl cli-props))
        map-par (assoc cli-props :asl_id (:asl_id (first asl)))]
    (if (empty? asl)
       (log/info (str "Nessuna asl trovata per regione " (:regione map-par) " e asl " (:asl map-par)))
       (do
         (log/info (str "Lavoriamo con asl = " (:asl_id (first asl))))
         (log/info (str "cli: " cli-res))
         (log/info (str "parametri: " map-par))
         (log/info (str "tutti: " (assoc map-par :asl_id (:asl_id (first asl)))))))))
         ;;(import-report (assoc map-par :asl_id (:asl_id (first asl))))))))
