package kpn.data.assignment.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Outage {
    String endDate;
    String title;
    String postalCodes;
    String status;
    String locations;
    String startDate;
    String description;
}
