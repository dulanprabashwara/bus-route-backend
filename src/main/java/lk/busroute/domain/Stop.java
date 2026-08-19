package lk.busroute.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "stops")
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_si")
    private String nameSi;

    @Column(name = "name_ta")
    private String nameTa;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(name = "district")
    private String district;

    @Column(name = "province")
    private String province;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "raw_corrupted_name")
    private String rawCorruptedName;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private ZonedDateTime updatedAt;

    public Stop() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getRawCorruptedName() { return rawCorruptedName; }
    public void setRawCorruptedName(String rawCorruptedName) { this.rawCorruptedName = rawCorruptedName; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getDisplayName() {
        if (nameEn != null && !nameEn.trim().isEmpty()) {
            return nameEn;
        }
        if (nameSi != null && !nameSi.trim().isEmpty()) {
            return nameSi;
        }
        if (nameTa != null && !nameTa.trim().isEmpty()) {
            return nameTa;
        }
        return normalizedName;
    }
}
