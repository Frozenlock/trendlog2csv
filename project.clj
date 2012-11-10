(defproject trendlog2csv "0.1.0-SNAPSHOT"
  :description "Simple application to retreive trend logs on a BACnet network and export them to a .csv file"
  :url "https://bacnethelp.com"
  :license {:name "GPLV3"
            :url "http://www.gnu.org/licenses/gpl-3.0.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [bacnet-scan-utils "1.0.7"]
                 [incanter "1.3.0"]
                 [org.clojure/tools.cli "0.2.2"]
                 [clojure-csv/clojure-csv "2.0.0-alpha1"]]
  :main trendlog2csv.core)