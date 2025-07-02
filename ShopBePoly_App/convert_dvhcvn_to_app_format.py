import json
import os

# Đường dẫn file nguồn và file đích
SOURCE_FILE = 'data.json'  # File tải từ https://raw.githubusercontent.com/dvhcvn/data/master/data.json
DEST_FILE = os.path.join('app', 'src', 'main', 'assets', 'vietnam_addresses.json')

# Đọc dữ liệu nguồn
with open(SOURCE_FILE, 'r', encoding='utf-8') as f:
    data = json.load(f)

# Nếu file là một list, giữ nguyên, nếu là dict, chuyển thành list
if isinstance(data, dict):
    data = [data]

# Chuẩn bị cấu trúc dữ liệu đích
dest = {"provinces": []}

for province in data:
    province_obj = {
        "name": province["name"],
        "code": province["level1_id"],
        "districts": []
    }
    for district in province.get("level2s", []):
        district_obj = {
            "name": district["name"],
            "code": district["level2_id"],
            "wards": []
        }
        for ward in district.get("level3s", []):
            ward_obj = {
                "name": ward["name"],
                "code": ward["level3_id"]
            }
            district_obj["wards"].append(ward_obj)
        province_obj["districts"].append(district_obj)
    dest["provinces"].append(province_obj)

# Ghi ra file đích
with open(DEST_FILE, 'w', encoding='utf-8') as f:
    json.dump(dest, f, ensure_ascii=False, indent=2)

print(f"Đã chuyển đổi xong! File kết quả: {DEST_FILE}") 