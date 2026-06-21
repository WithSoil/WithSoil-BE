package com.outthedoor.withsoil.api.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberLocation {

    @Column(name = "location_sido", length = 20)
    private String sido;

    @Column(name = "location_sigungu", length = 30)
    private String sigungu;

    @Column(name = "location_eup_myeon_dong", length = 30)
    private String eupMyeonDong;

    @Column(name = "location_ri", length = 30)
    private String ri;

    @Column(name = "location_latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "location_longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    public static MemberLocation of(String sido, String sigungu, String eupMyeonDong,
                                    String ri, BigDecimal latitude, BigDecimal longitude) {
        MemberLocation location = new MemberLocation();
        location.sido = sido;
        location.sigungu = sigungu;
        location.eupMyeonDong = eupMyeonDong;
        location.ri = ri;
        location.latitude = latitude;
        location.longitude = longitude;
        return location;
    }
}
