(ns md-specialista-import.services.mailer
  (:require
    [md-specialista-import.services.database-asl :as db-asl]
    [postal.core :as postal]))

(def smtp-auth
  {:host "mail.marnonet.it"
   :port 587
   :user "supporto@medicodigitale.it"
   :pass "M4rn0@123"
   :tls true})

(defn send-mail [m]
   (postal/send-message
      smtp-auth
      {:from "supporto@medicodigitale.it"
       :to m
       :subject "[MEDICO DIGITALE] Notifica disponibilità report specialisti"
       :body "Report specialisti disponibili per l'approvazione su medicodigitale.it/specialista"}))

(defn notifica-farmacisti [asl_id]
 (->>
   (db-asl/get-farmacisti asl_id)
   (map #(send-mail(:utente_mail %)))))
