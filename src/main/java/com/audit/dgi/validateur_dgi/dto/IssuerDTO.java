package com.audit.dgi.validateur_dgi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuerDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    @Pattern(regexp = "^\\d{15}$", message = "L'ICE émetteur doit contenir exactement 15 chiffres")
    private String ice;

    @NotBlank
    @Pattern(regexp = "^\\d{6,8}$", message = "L'IF émetteur doit contenir 6 à 8 chiffres")
    private String ifNumber;

    private String patente;

    private String rc;

    private String cnss;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getIce() { return ice; }
    public void setIce(String ice) { this.ice = ice; }
    public String getIfNumber() { return ifNumber; }
    public void setIfNumber(String ifNumber) { this.ifNumber = ifNumber; }
    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }
    public String getRc() { return rc; }
    public void setRc(String rc) { this.rc = rc; }
    public String getCnss() { return cnss; }
    public void setCnss(String cnss) { this.cnss = cnss; }
}
