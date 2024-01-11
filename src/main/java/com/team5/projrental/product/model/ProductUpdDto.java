package com.team5.projrental.product.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team5.projrental.product.model.innermodel.StoredFileInfo;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductUpdDto {

    @NotNull
    @Min(1)
    private Integer iproduct;
    @NotNull
    @Min(1)
    private Integer iuser;
    private String category;
    private String addr;
    private String restAddr;
    private String title;
    private String contents;
    private MultipartFile mainPic;
    private List<MultipartFile> pics;
    @Range(min = 100, max = Integer.MAX_VALUE)
    private Integer price;
    @Range(min = 100, max = Integer.MAX_VALUE)
    private Integer rentalPrice;
    @Range(min = 70, max = 100)
    private Integer depositPer;
    private LocalDate buyDate;
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;

    // add
    @Size(max = 9)
    List<Integer> delPics;


    @JsonIgnore
    private StoredFileInfo storedMainPic;
    @JsonIgnore
    private Integer iaddr;
    @JsonIgnore
    private Integer icategory;
    @JsonIgnore
    private Integer deposit;
    @JsonIgnore
    private Double x;
    @JsonIgnore
    private Double y;
}
