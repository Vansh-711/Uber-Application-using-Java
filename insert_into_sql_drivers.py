import pandas as pd
import random

# Load CSV (tab-separated, not comma)
cities_df = pd.read_csv("india_cities_clean2.csv", sep=",")
cities_df.columns = cities_df.columns.str.strip().str.lower()
print(cities_df.columns.tolist())  # should now be ['city', 'lat', 'lng', 'population']

# Sample names
first_names = ["Amit", "Ravi", "Vikram", "Sunil", "Anil", "Rajesh", "Deepak", "Arjun", "Sanjay", "Manoj",
               "Priya", "Anita", "Kavita", "Neha", "Pooja", "Shreya", "Divya", "Sneha", "Kiran", "Ritu"]
last_names = ["Kumar", "Sharma", "Verma", "Gupta", "Mehta", "Reddy", "Patel", "Yadav", "Singh", "Das"]
genders = ["Male"] * 7 + ["Female"] * 3  # skewed more towards male

# Vehicle types - 5 drivers for each type per city
vehicles = ["Two-wheeler", "Rickshaw", "Normal cab", "Cab XL", "Premium Cab"]

# SQL dump start
sql_dump = """CREATE TABLE drivers (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    gender VARCHAR(10),
    rating INT,
    city VARCHAR(100),
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    vehicle VARCHAR(50)
);\n\n"""

insert_statements = []
id_counter = 1

# Generate 25 drivers per city (5 for each vehicle type)
for _, row in cities_df.iterrows():
    try:
        lat = float(row['lat'])
        lng = float(row['lng'])
    except ValueError:
        continue  # skip invalid rows

    city = str(row['city']).replace("'", "''")  # escape quotes

    # Generate 5 drivers for each vehicle type
    for vehicle in vehicles:
        for _ in range(5):
            name = f"{random.choice(first_names)} {random.choice(last_names)}"
            age = random.randint(18, 60)
            gender = random.choice(genders)
            rating = random.randint(1, 10)

            insert_statements.append(
                f"({id_counter}, '{name}', {age}, '{gender}', {rating}, '{city}', {lat:.6f}, {lng:.6f}, '{vehicle}')"
            )
            id_counter += 1

# Write all INSERTS
sql_dump += "INSERT INTO drivers (id, name, age, gender, rating, city, latitude, longitude, vehicle) VALUES\n"
sql_dump += ",\n".join(insert_statements) + ";\n"

with open("drivers_dump.sql", "w", encoding="utf-8") as f:
    f.write(sql_dump)

print("✅ SQL dump generated: drivers_dump.sql")
print(f"Total drivers generated: {id_counter - 1}")
print(f"Cities processed: {len(cities_df)}")
print(f"Drivers per city: 25 (5 for each vehicle type)")
print(f"Vehicle types: {', '.join(vehicles)}")