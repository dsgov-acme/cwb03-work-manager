package io.nuvalence.workmanager.service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonAddress {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateCode;
    private String postalCode;
    private String postalCodeExtension;
    private String countryCode;
}
