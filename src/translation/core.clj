(ns translation.core
  (:require [amazonica.aws.translate :as awst]))

(def supported-languages
  {:afrikaans :af
   :albanian :sq
   :amharic :am
   :arabic :ar
   :azerbaijani	:az
   :bengali :bn
   :bosnian :bs
   :bulgarian :bg
   :chinese-simplified :zh
   :chinese-traditional :zh-TW
   :croatian	:hr
   :czech :cs
   :danish :da
   :dari	:fa-AF
   :dutch :nl
   :english :en
   :estonian :et
   :finnish :fi
   :french :fr
   :french-canada :fr-CA
   :georgian :ka
   :german :de
   :greek :el
   :hausa :ha
   :hebrew :he
   :hindi :hi
   :hungarian :hu
   :indonesian :id
   :italian :it
   :japanese :ja
   :korean :ko
   :latvian :lv
   :malay :ms
   :norwegian :no
   :persian :fa
   :pashto :ps
   :polish :pl
   :portuguese :pt
   :pomanian :ro
   :russian :ru
   :serbian :sr
   :slovak :sk
   :slovenian :sl
   :somali :so
   :spanish :es
   :spanish-mexico :es-MX
   :swahili :sw
   :swedish :sv
   :tagalog :tl
   :tamil :ta
   :thai :th
   :turkish :tr
   :ukrainian :uk
   :urdu :ur
   :vietnamese :vi})


(defn translate
  [{:keys [text target-language]}]
  (->> (awst/translate-text
         {:source-language-code "en"
          :target-language-code
          (supported-languages target-language)
          :text text})
    :translated-text
    (assoc {:src-text text
            :language target-language}
      :translation)))


(comment

  (translate {:text "Hello, World" :target-language :swedish})

  )
