(ns oven-mitts.backend.api-requests
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [oven-mitts.backend.nlp :as nlp])
  (:import [java.net URLEncoder]))

(def punk-api-url
  "Base URL for the Brewdog Punk API. See https://punkapi.com for documentation."
  "https://api.punkapi.com/v2")

(def recipe-puppy-api-url
  "Base URL for the Recipe Puppy API. See http://www.recipepuppy.com/about/api/ for documentation."
  "http://www.recipepuppy.com/api/")

(def random-beer-url (str punk-api-url "/beers/random"))

(defn json->map [s]
  (json/read-str s
                 :key-fn keyword))

(defn punk-api-get-request [url]
  (let [response (future (client/get url
                                     {:as :json
                                      :throw-exceptions false}))]
    (json->map (:body @response))))

(defn get-random-beer
  []
  (punk-api-get-request random-beer-url))


(defn get-beers-boozier-than [abv]
  (let [url  (str punk-api-url "/beers?abv_gt=" (Integer/parseInt abv))]
    (punk-api-get-request url)))

(defn get-beer-by-id [id]
  (let [url (str punk-api-url "/beers/" (Integer/parseInt id))]
    (punk-api-get-request url)))

(defn get-beers-by-partial-name [name]
  (let [url  (str punk-api-url "/beers?beer_name=" name)]
    (punk-api-get-request url)))

(defn get-recipe-by-foods [foods]
  (let [food-query (clojure.string/join "," foods)]
    (future (client/get recipe-puppy-api-url
                        {:as :json
                         :query-params {"q" food-query}
                         :throw-exceptions false}))))

(defn get-random-beer-and-food-pairing
  []
  (let [random-beer (first (get-random-beer))
        food-query  (nlp/generate-ingredient-list (first (:food_pairing random-beer)))
        response    (get-recipe-by-foods food-query)]
    {:beer random-beer
     :food-query food-query
     :recipe (-> @response
                 :body
                 (json->map)
                 :results
                 (first))}))
