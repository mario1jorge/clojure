(ns crud-api.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [compojure.route :as route]
            [ring.util.response :refer [response status]]
            [cheshire.core :as json]))

(def items (atom {}))

(defn get-items [_]
  (response (vals @items)))

(defn get-item [id]
  (let [item (get @items id)]
    (if item
      (response item)
      (status (response {:error "Item not found"}) 404))))

(defn create-item [req]
  (let [item (json/parse-string (slurp (:body req)) true)]
    (if (contains? @items (:id item))
      (status (response {:error "Item already exists"}) 400)
      (do
        (swap! items assoc (:id item) item)
        (status (response item) 201)))))

(defn update-item [id req]
  (let [item (json/parse-string (slurp (:body req)) true)]
    (if (contains? @items id)
      (do
        (swap! items assoc id (assoc item :id id))
        (response item))
      (status (response {:error "Item not found"}) 404))))

(defn delete-item [id]
  (if (contains? @items id)
    (do
      (swap! items dissoc id)
      (status (response nil) 204))
    (status (response {:error "Item not found"}) 404))))

(defroutes app
  (GET "/items" [] get-items)
  (GET "/items/:id" [id] (get-item id))
  (POST "/items" req (create-item req))
  (PUT "/items/:id" [id :as req] (update-item id req))
  (DELETE "/items/:id" [id] (delete-item id))
  (route/not-found (response {:error "Not found"})))

(defn -main []
  (run-jetty app {:port 3000 :join? false}))
