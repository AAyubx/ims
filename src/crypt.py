import bcrypt

pw = b"TempPassword123!"
new_hash = bcrypt.hashpw(pw, bcrypt.gensalt(12)).decode()  # string like $2b$12$...
print(new_hash)

