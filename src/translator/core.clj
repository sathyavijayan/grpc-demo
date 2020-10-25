(ns translator.core
  (:require [amazonica.aws.translate :as awst]))

(def supported-languages
  #{:ps :pt :da :ja :es-MX :ha :no :hi :am :ta
   :cs :sr :bn :sw :id :bs :fa-AF :ur :sk :bg
   :ru :nl :ms :uk :et :fi :so :vi :tr :th :it
   :hr :az :zh :sq :ro :sl :en :fa :he :ka :tl
   :sv :af :ko :de :el :es :fr :ar :hu :pl :zh-TW
   :fr-CA :lv})

(defn translate
  [{:keys [text target-language]}]
  (->> (awst/translate-text
         {:source-language-code "en"
          :target-language-code target-language
          :text text})
    :translated-text
    (assoc {:src-text text
            :language target-language}
      :translation)))

(comment

  (translate {:text "Hello, World" :target-language "ta"})


  )
