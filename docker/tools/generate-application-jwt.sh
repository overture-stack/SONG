curl --location --request POST 'http://localhost:9082/oauth/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'client_id=ego' \
--data-urlencode 'client_secret=ego'
