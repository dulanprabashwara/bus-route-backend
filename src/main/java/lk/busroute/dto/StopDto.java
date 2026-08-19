package lk.busroute.dto;

import lk.busroute.domain.Stop;

public class StopDto {
    private Long id;
    private String name;
    private String nameEn;
    private String nameSi;
    private String nameTa;
    private String normalizedName;
    private String district;
    private String province;
    private Double latitude;
    private Double longitude;

    public StopDto() {}

    public StopDto(Long id, String name, String nameEn, String nameSi, String nameTa, String normalizedName, String district, String province, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.nameEn = nameEn;
        this.nameSi = nameSi;
        this.nameTa = nameTa;
        this.normalizedName = normalizedName;
        this.district = district;
        this.province = province;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static StopDto fromEntity(Stop stop) {
        if (stop == null) return null;
        return new StopDto(
                stop.getId(),
                stop.getDisplayName(),
                stop.getNameEn(),
                stop.getNameSi(),
                stop.getNameTa(),
                stop.getNormalizedName(),
                stop.getDistrict(),
                stop.getProvince(),
                stop.getLatitude(),
                stop.getLongitude()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameSi() { return nameSi; }
    public void setNameSi(String nameSi) { this.nameSi = nameSi; }

    public String getNameTa() { return nameTa; }
    public void setNameTa(String nameTa) { this.nameTa = nameTa; }

    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
