echo " "
echo "      === Создание записей ==="
echo "Пассажиры:"
curl -X POST http://localhost:8081/passengers \
    -H "Content-Type: application/json" \
    -d '{
    "name":"Stas",
    "email":"stas@email.com",
    "phone":"+79876543210"
    }'
echo " "
curl -X POST http://localhost:8081/passengers \
    -H "Content-Type: application/json" \
    -d '{
    "name":"Igor",
    "email":"egor@letov.ru",
    "phone":"+79998887766"
    }'
echo " "
curl -X POST http://localhost:8081/passengers \
    -H "Content-Type: application/json" \
    -d '{
    "name":"Andrew",
    "email":"andrew@code.dom",
    "phone":"+70000000000"
    }'
echo " "
sleep 3
echo "Водители:"
curl -X POST http://localhost:8081/drivers \
    -H "Content-Type: application/json" \
    -d '{
    "name":"Rain",
    "email":"gos@uslugi.ru",
    "phone":"+79870126534",
    "licenseNumber":"liNu123"
    }'
echo " "
curl -X POST http://localhost:8081/drivers \
    -H "Content-Type: application/json" \
    -d '{
    "name":"Ator",
    "email":"ter@mi.n",
    "phone":"+79999999999",
    "licenseNumber":"TRMN999"
    }'
echo " "
curl -X POST http://localhost:8081/drivers \
    -H "Content-Type: application/json" \
    -d '{
    "name":"Driver",
    "email":"micro@soft.com",
    "phone":"+71312425678",
    "licenseNumber":"MSFT990"
    }'
echo " "
sleep 3
echo "Поездки:"
curl -X POST http://localhost:8082/trips \
    -H "Content-Type: application/json" \
    -d '{
    "passengerId":1,
    "origin":"A",
    "destination":"B",
    "distance_km":15,
    "rating":5,
    "feedback":"qwerty"
    }'
echo " "
curl -X POST http://localhost:8082/trips \
    -H "Content-Type: application/json" \
    -d '{
    "passengerId":2,
    "origin":"Here",
    "destination":"LHL"
    }'
echo " "
sleep 3
echo "Оценка:"
curl -X POST http://localhost:8082/trips/1/rate \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "feedback": "Отличный водитель"
  }'
echo " "
sudo docker exec -it taxi-postgres psql -U taxi_user -d taxi_db