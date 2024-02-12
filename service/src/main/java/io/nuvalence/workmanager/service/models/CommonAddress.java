package io.nuvalence.workmanager.service.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommonAddress {
    private String address1;
    private String address2;
    private String city;
    private String stateCode;
    private String postalCode;
    private String postalCodeExtension;
    private String countryCode;
}
