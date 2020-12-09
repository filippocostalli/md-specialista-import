(defproject md-specialista-import "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cprop "0.1.16"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.9"]
                 [com.microsoft.sqlserver/mssql-jdbc "8.3.1.jre8-preview"]
                 [clj-time "0.15.2"]
                 [org.clojure/tools.cli "1.0.194"]
                 [cambium/cambium.core "0.9.3"]
                 [cambium/cambium.codec-simple "0.9.3"]
                 [cambium/cambium.logback.core "0.4.3"]
                 [digest "1.4.9"]]
  :jvm-opts ["-Xmx2g"]
  :main ^:skip-aot md-specialista-import.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
