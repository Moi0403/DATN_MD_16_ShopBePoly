package com.example.shopbepoly.DTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VietnamAddress {
    private String name;
    private String code;
    private Map<String, District> districts;

    public VietnamAddress() {
        districts = new HashMap<>();
    }

    public VietnamAddress(String name, String code) {
        this.name = name;
        this.code = code;
        this.districts = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, District> getDistricts() {
        return districts;
    }

    public void setDistricts(Map<String, District> districts) {
        this.districts = districts;
    }

    public static class District {
        private String name;
        private String code;
        private Map<String, Ward> wards;

        public District() {
            wards = new HashMap<>();
        }

        public District(String name, String code) {
            this.name = name;
            this.code = code;
            this.wards = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Map<String, Ward> getWards() {
            return wards;
        }

        public void setWards(Map<String, Ward> wards) {
            this.wards = wards;
        }
    }

    public static class Ward {
        private String name;
        private String code;

        public Ward() {
        }

        public Ward(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    // Dữ liệu các tỉnh thành Việt Nam
    public static List<VietnamAddress> getProvinces() {
        List<VietnamAddress> provinces = new ArrayList<>();
        
        // Hà Nội
        VietnamAddress haNoi = new VietnamAddress("Hà Nội", "01");
        haNoi.getDistricts().put("001", new District("Ba Đình", "001"));
        haNoi.getDistricts().put("002", new District("Hoàn Kiếm", "002"));
        haNoi.getDistricts().put("003", new District("Tây Hồ", "003"));
        haNoi.getDistricts().put("004", new District("Long Biên", "004"));
        haNoi.getDistricts().put("005", new District("Cầu Giấy", "005"));
        haNoi.getDistricts().put("006", new District("Đống Đa", "006"));
        haNoi.getDistricts().put("007", new District("Hai Bà Trưng", "007"));
        haNoi.getDistricts().put("008", new District("Hoàng Mai", "008"));
        haNoi.getDistricts().put("009", new District("Thanh Xuân", "009"));
        haNoi.getDistricts().put("016", new District("Sóc Sơn", "016"));
        haNoi.getDistricts().put("017", new District("Đông Anh", "017"));
        haNoi.getDistricts().put("018", new District("Gia Lâm", "018"));
        haNoi.getDistricts().put("019", new District("Nam Từ Liêm", "019"));
        haNoi.getDistricts().put("020", new District("Thanh Trì", "020"));
        haNoi.getDistricts().put("021", new District("Bắc Từ Liêm", "021"));
        haNoi.getDistricts().put("250", new District("Mê Linh", "250"));
        haNoi.getDistricts().put("268", new District("Hà Đông", "268"));
        haNoi.getDistricts().put("269", new District("Sơn Tây", "269"));
        haNoi.getDistricts().put("271", new District("Ba Vì", "271"));
        haNoi.getDistricts().put("272", new District("Phúc Thọ", "272"));
        haNoi.getDistricts().put("273", new District("Đan Phượng", "273"));
        haNoi.getDistricts().put("274", new District("Hoài Đức", "274"));
        haNoi.getDistricts().put("275", new District("Quốc Oai", "275"));
        haNoi.getDistricts().put("276", new District("Thạch Thất", "276"));
        haNoi.getDistricts().put("277", new District("Chương Mỹ", "277"));
        haNoi.getDistricts().put("278", new District("Thanh Oai", "278"));
        haNoi.getDistricts().put("279", new District("Thường Tín", "279"));
        haNoi.getDistricts().put("280", new District("Phú Xuyên", "280"));
        haNoi.getDistricts().put("281", new District("Ứng Hòa", "281"));
        haNoi.getDistricts().put("282", new District("Mỹ Đức", "282"));
        provinces.add(haNoi);

        // TP. Hồ Chí Minh
        VietnamAddress hcm = new VietnamAddress("TP. Hồ Chí Minh", "79");
        hcm.getDistricts().put("760", new District("Quận 1", "760"));
        hcm.getDistricts().put("761", new District("Quận 12", "761"));
        hcm.getDistricts().put("762", new District("Quận Thủ Đức", "762"));
        hcm.getDistricts().put("763", new District("Quận 9", "763"));
        hcm.getDistricts().put("764", new District("Quận Gò Vấp", "764"));
        hcm.getDistricts().put("765", new District("Quận Bình Thạnh", "765"));
        hcm.getDistricts().put("766", new District("Quận Tân Bình", "766"));
        hcm.getDistricts().put("767", new District("Quận Tân Phú", "767"));
        hcm.getDistricts().put("768", new District("Quận Phú Nhuận", "768"));
        hcm.getDistricts().put("769", new District("Quận 2", "769"));
        hcm.getDistricts().put("770", new District("Quận 3", "770"));
        hcm.getDistricts().put("771", new District("Quận 10", "771"));
        hcm.getDistricts().put("772", new District("Quận 11", "772"));
        hcm.getDistricts().put("773", new District("Quận 4", "773"));
        hcm.getDistricts().put("774", new District("Quận 5", "774"));
        hcm.getDistricts().put("775", new District("Quận 6", "775"));
        hcm.getDistricts().put("776", new District("Quận 8", "776"));
        hcm.getDistricts().put("777", new District("Quận Bình Tân", "777"));
        hcm.getDistricts().put("778", new District("Quận 7", "778"));
        hcm.getDistricts().put("783", new District("Huyện Củ Chi", "783"));
        hcm.getDistricts().put("784", new District("Huyện Hóc Môn", "784"));
        hcm.getDistricts().put("785", new District("Huyện Bình Chánh", "785"));
        hcm.getDistricts().put("786", new District("Huyện Nhà Bè", "786"));
        hcm.getDistricts().put("787", new District("Huyện Cần Giờ", "787"));
        provinces.add(hcm);

        // Đà Nẵng
        VietnamAddress daNang = new VietnamAddress("Đà Nẵng", "48");
        daNang.getDistricts().put("490", new District("Quận Liên Chiểu", "490"));
        daNang.getDistricts().put("491", new District("Quận Thanh Khê", "491"));
        daNang.getDistricts().put("492", new District("Quận Hải Châu", "492"));
        daNang.getDistricts().put("493", new District("Quận Sơn Trà", "493"));
        daNang.getDistricts().put("494", new District("Quận Ngũ Hành Sơn", "494"));
        daNang.getDistricts().put("495", new District("Quận Cẩm Lệ", "495"));
        daNang.getDistricts().put("497", new District("Huyện Hòa Vang", "497"));
        daNang.getDistricts().put("498", new District("Huyện Hoàng Sa", "498"));
        provinces.add(daNang);

        // Cần Thơ
        VietnamAddress canTho = new VietnamAddress("Cần Thơ", "92");
        canTho.getDistricts().put("916", new District("Quận Ninh Kiều", "916"));
        canTho.getDistricts().put("917", new District("Quận Ô Môn", "917"));
        canTho.getDistricts().put("918", new District("Quận Bình Thủy", "918"));
        canTho.getDistricts().put("919", new District("Quận Cái Răng", "919"));
        canTho.getDistricts().put("923", new District("Quận Thốt Nốt", "923"));
        canTho.getDistricts().put("924", new District("Huyện Vĩnh Thạnh", "924"));
        canTho.getDistricts().put("925", new District("Huyện Cờ Đỏ", "925"));
        canTho.getDistricts().put("926", new District("Huyện Phong Điền", "926"));
        canTho.getDistricts().put("927", new District("Huyện Thới Lai", "927"));
        provinces.add(canTho);

        // Hải Phòng
        VietnamAddress haiPhong = new VietnamAddress("Hải Phòng", "31");
        haiPhong.getDistricts().put("303", new District("Quận Hồng Bàng", "303"));
        haiPhong.getDistricts().put("304", new District("Quận Ngô Quyền", "304"));
        haiPhong.getDistricts().put("305", new District("Quận Lê Chân", "305"));
        haiPhong.getDistricts().put("306", new District("Quận Hải An", "306"));
        haiPhong.getDistricts().put("307", new District("Quận Kiến An", "307"));
        haiPhong.getDistricts().put("308", new District("Quận Đồ Sơn", "308"));
        haiPhong.getDistricts().put("309", new District("Quận Dương Kinh", "309"));
        haiPhong.getDistricts().put("311", new District("Huyện Thuỷ Nguyên", "311"));
        haiPhong.getDistricts().put("312", new District("Huyện An Dương", "312"));
        haiPhong.getDistricts().put("313", new District("Huyện An Lão", "313"));
        haiPhong.getDistricts().put("314", new District("Huyện Kiến Thuỵ", "314"));
        haiPhong.getDistricts().put("315", new District("Huyện Tiên Lãng", "315"));
        haiPhong.getDistricts().put("316", new District("Huyện Vĩnh Bảo", "316"));
        haiPhong.getDistricts().put("317", new District("Huyện Cát Hải", "317"));
        haiPhong.getDistricts().put("318", new District("Huyện Bạch Long Vĩ", "318"));
        provinces.add(haiPhong);

        return provinces;
    }

    // Lấy danh sách quận/huyện theo tỉnh
    public static List<District> getDistrictsByProvince(String provinceCode) {
        List<VietnamAddress> provinces = getProvinces();
        for (VietnamAddress province : provinces) {
            if (province.getCode().equals(provinceCode)) {
                return new ArrayList<>(province.getDistricts().values());
            }
        }
        return new ArrayList<>();
    }

    // Lấy danh sách xã/phường theo quận/huyện (mẫu dữ liệu)
    public static List<Ward> getWardsByDistrict(String districtCode) {
        List<Ward> wards = new ArrayList<>();
        
        // Thêm một số xã/phường mẫu cho các quận/huyện chính
        if (districtCode.equals("001")) { // Ba Đình
            wards.add(new Ward("Phường Phúc Xá", "00001"));
            wards.add(new Ward("Phường Trúc Bạch", "00004"));
            wards.add(new Ward("Phường Vĩnh Phúc", "00006"));
            wards.add(new Ward("Phường Cống Vị", "00007"));
            wards.add(new Ward("Phường Liễu Giai", "00008"));
            wards.add(new Ward("Phường Nguyễn Trung Trực", "00010"));
            wards.add(new Ward("Phường Quán Thánh", "00013"));
            wards.add(new Ward("Phường Ngọc Hà", "00016"));
            wards.add(new Ward("Phường Điện Biên", "00019"));
            wards.add(new Ward("Phường Đội Cấn", "00022"));
            wards.add(new Ward("Phường Ngọc Khánh", "00025"));
            wards.add(new Ward("Phường Kim Mã", "00028"));
            wards.add(new Ward("Phường Giảng Võ", "00031"));
            wards.add(new Ward("Phường Thành Công", "00034"));
        } else if (districtCode.equals("002")) { // Hoàn Kiếm
            wards.add(new Ward("Phường Phúc Tân", "00037"));
            wards.add(new Ward("Phường Đồng Xuân", "00040"));
            wards.add(new Ward("Phường Hàng Mã", "00043"));
            wards.add(new Ward("Phường Hàng Buồm", "00046"));
            wards.add(new Ward("Phường Hàng Đào", "00049"));
            wards.add(new Ward("Phường Hàng Bồ", "00052"));
            wards.add(new Ward("Phường Cửa Đông", "00055"));
            wards.add(new Ward("Phường Lý Thái Tổ", "00058"));
            wards.add(new Ward("Phường Hàng Bạc", "00061"));
            wards.add(new Ward("Phường Hàng Gai", "00064"));
            wards.add(new Ward("Phường Chương Dương", "00067"));
            wards.add(new Ward("Phường Hàng Trống", "00070"));
            wards.add(new Ward("Phường Cửa Nam", "00073"));
            wards.add(new Ward("Phường Hàng Bông", "00076"));
            wards.add(new Ward("Phường Tràng Tiền", "00079"));
            wards.add(new Ward("Phường Trần Hưng Đạo", "00082"));
            wards.add(new Ward("Phường Phan Chu Trinh", "00085"));
            wards.add(new Ward("Phường Hàng Bài", "00088"));
        } else if (districtCode.equals("760")) { // Quận 1
            wards.add(new Ward("Phường Bến Nghé", "26734"));
            wards.add(new Ward("Phường Bến Thành", "26737"));
            wards.add(new Ward("Phường Cầu Kho", "26740"));
            wards.add(new Ward("Phường Cầu Ông Lãnh", "26743"));
            wards.add(new Ward("Phường Cô Giang", "26746"));
            wards.add(new Ward("Phường Đa Kao", "26749"));
            wards.add(new Ward("Phường Nguyễn Cư Trinh", "26752"));
            wards.add(new Ward("Phường Nguyễn Thái Bình", "26755"));
            wards.add(new Ward("Phường Phạm Ngũ Lão", "26758"));
            wards.add(new Ward("Phường Tân Định", "26761"));
        } else if (districtCode.equals("770")) { // Quận 3
            wards.add(new Ward("Phường 01", "26764"));
            wards.add(new Ward("Phường 02", "26767"));
            wards.add(new Ward("Phường 03", "26770"));
            wards.add(new Ward("Phường 04", "26773"));
            wards.add(new Ward("Phường 05", "26776"));
            wards.add(new Ward("Phường 06", "26779"));
            wards.add(new Ward("Phường 07", "26782"));
            wards.add(new Ward("Phường 08", "26785"));
            wards.add(new Ward("Phường 09", "26788"));
            wards.add(new Ward("Phường 10", "26791"));
            wards.add(new Ward("Phường 11", "26794"));
            wards.add(new Ward("Phường 12", "26797"));
            wards.add(new Ward("Phường 13", "26800"));
            wards.add(new Ward("Phường 14", "26803"));
        } else if (districtCode.equals("492")) { // Quận Hải Châu - Đà Nẵng
            wards.add(new Ward("Phường Thạch Thang", "20305"));
            wards.add(new Ward("Phường Hải Châu I", "20308"));
            wards.add(new Ward("Phường Hải Châu II", "20311"));
            wards.add(new Ward("Phường Phước Ninh", "20314"));
            wards.add(new Ward("Phường Hòa Thuận Tây", "20317"));
            wards.add(new Ward("Phường Hòa Thuận Đông", "20320"));
            wards.add(new Ward("Phường Nam Dương", "20323"));
            wards.add(new Ward("Phường Bình Hiên", "20326"));
            wards.add(new Ward("Phường Bình Thuận", "20329"));
            wards.add(new Ward("Phường Hòa Cường Bắc", "20332"));
            wards.add(new Ward("Phường Hòa Cường Nam", "20333"));
        } else {
            // Thêm xã/phường mẫu cho các quận/huyện khác
            wards.add(new Ward("Phường/Xã 1", "00001"));
            wards.add(new Ward("Phường/Xã 2", "00002"));
            wards.add(new Ward("Phường/Xã 3", "00003"));
            wards.add(new Ward("Phường/Xã 4", "00004"));
            wards.add(new Ward("Phường/Xã 5", "00005"));
            wards.add(new Ward("Phường/Xã 6", "00006"));
            wards.add(new Ward("Phường/Xã 7", "00007"));
            wards.add(new Ward("Phường/Xã 8", "00008"));
            wards.add(new Ward("Phường/Xã 9", "00009"));
            wards.add(new Ward("Phường/Xã 10", "00010"));
        }
        
        return wards;
    }
} 