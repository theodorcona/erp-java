# Run the application locally

```
// From project directory
docker compose up --no-deps --build
```

Your application will now be available at `localhost:6868`:

### Testing the application
You can create any entity by specifying its entity name and schema,
and subsequently create entities of that type

See deployed openapi file at http://localhost:6868/swagger-ui/index.html
for available API

Below is a simple example of creating an entity called 'User' and then creating
an instance of it. You can import curl commands into postman

#### Create an entity 'User'
```
curl --location 'localhost:6868/entities' \
--header 'Content-Type: application/json' \
--data '{
    "entityName": "User",
    "schema": {
        "properties": [
            {
                "key": "address",
                "type": "OBJECT",
                "objectProperties": [
                    {
                        "key": "street",
                        "type": "STRING"
                    },
                    {
                        "key": "city",
                        "type": "STRING"
                    },
                    {
                        "key": "number",
                        "type": "LONG"
                    }
                ]
            },
            {
                "key": "email",
                "type": "STRING"
            }
        ]
    }
}'
```

#### Create a User
```
curl --location 'localhost:6868/entities/User' \
--header 'Content-Type: application/json' \
--data-raw '{
    "data": {
        "address": {
            "street": "zarquon",
            "city": "zenquin",
            "number": 42
        },
        "email": "zaphod@beeblebrox.com"
    }
}'
```

#### Get all users
```
curl --location 'localhost:6868/entities/User?pageSize=1'```
```