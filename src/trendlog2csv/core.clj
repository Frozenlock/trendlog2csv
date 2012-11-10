(ns trendlog2csv.core
  (:gen-class :main true)
  (:require [bacnet-scan-utils.bacnet :as b]
            [bacnet-scan-utils.export :as exp]
            [clojure-csv.core :as csv])
  (:use [clojure.tools.cli :only [cli]]))

(import '(com.serotonin.bacnet4j
          RemoteDevice
          type.enumerated.ObjectType
          type.primitive.ObjectIdentifier))



(defn maps->table
  "Transform a collection of maps into a table ready for csv export."
  [maps]
  (let [ks (distinct (mapcat keys maps))]
    (apply map list
           (for [k ks] (cons (name k) (map k maps))))))

(defn maps->csv [maps]
  (when maps
    (let [table (maps->table maps)
          csv (csv/write-csv table)]
      csv)))



(defn get-remote-trend-log [device-id trend-log-instance]
  (b/with-local-device (b/new-local-device)
    (b/find-remote-devices)
    (-> (.getRemoteDevice b/local-device 1234)
        (b/get-trend-log-data (ObjectIdentifier. ObjectType/trendLog 0)))))

;;;; Command line options ;;;;;

(defn cmd-line [args]
  (let [[options args banner]
        (cli args
             ["-b" "--broadcast-address"
              "Broadcast address to send the \"Who is\""
              :default (b/get-broadcast-address)] ;IP as string

             ["-id" "--device-id" "The device instance for the local device"
              :default 1337 :parse-fn #(Integer. %)]

             ["-p" "--port" "The port on which the BACnet network is listening"
              :default 47808 :parse-fn #(Integer. %)]

             ["-rd" "--remote-id" "Remote device ID"
              :default false :parse-fn #(Integer. %)]

             ["-i" "--instance" "Target trend log instance"
              :default false :parse-fn #(Integer. %)]
             
             ["-f" "--export-filename" "Export filename"
              :default (exp/get-filename "BACnetHelp-Trend-log" ".csv")]

             ["-h" "--help" "Show help" :default false :flag true])
        
        bc-add (:broadcast-address options)
        filename (:export-filename options)
        id (:device-id options)
        port (:port options)
        remote-device (:remote-id options)
        instance (:instance options)]

    (println
     "
  ____          _____            _   _    _      _                           
 |  _ \\   /\\   / ____|          | | | |  | |    | |                          
 | |_) | /  \\ | |     _ __   ___| |_| |__| | ___| |_ __   ___ ___  _ __ ___  
 |  _ < / /\\ \\| |    | '_ \\ / _ \\ __|  __  |/ _ \\ | '_ \\ / __/ _ \\| '_ ` _ \\ 
 | |_) / ____ \\ |____| | | |  __/ |_| |  | |  __/ | |_) | (_| (_) | | | | | |
 |____/_/    \\_\\_____|_| |_|\\___|\\__|_|  |_|\\___|_| .__(_)___\\___/|_| |_| |_|
                                                  | |                        
                                                  |_|
"
     "Trend logs retrieval utility. Brought to you by BACnetHelp.com.

Open source under the GPLV3 licence. https://github.com/Frozenlock/trendlog2csv")
    
    (when (:help options)
      (println banner)
      (System/exit 0))

    (when-not (and remote-device instance)
      (println "You must specify a target device and trend log instance. For example, if you want to get the data from the trend log #10 in the device #10100, you should use the following:
\"<application call> -rd 10100 -i 10\"")
      (System/exit 0))
    

    (let [data (b/with-local-device (b/new-local-device
                                     :device-id id
                                     :broadcast-address bc-add
                                     :port port)
                 (b/find-remote-devices {:dest-port port})
                 (-> (.getRemoteDevice b/local-device remote-device)
                     (b/get-trend-log-data (ObjectIdentifier. ObjectType/trendLog instance))
                     (maps->csv)))]
      (spit filename data)
      (println (str "File exported as : "filename)))))


(defn -main [& args]
  (cmd-line args))