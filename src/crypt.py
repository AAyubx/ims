import bcrypt
print(bcrypt.hashpw(b"*Password123!", bcrypt.gensalt(12)).decode())