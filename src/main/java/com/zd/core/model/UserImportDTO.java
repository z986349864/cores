package com.zd.core.model;

import com.zd.core.excel.annotation.*;
import com.zd.core.excel.entity.RowBase;
import lombok.ToString;

@ToString
@ExcelData
public class UserImportDTO extends RowBase {
    @Required
    @Header(
            name = "id",
            columnNo = 1,
            comment = "id"
    )
    private String id;
    @Required
    @Length(
            min = 1,
            max = 50
    )
    @Header(
            name = "姓名",
            columnNo = 2,
            comment = "姓名"
    )
    private String name;
    /*@Header(
            name = "hscode.batch.import.header.hscode",
            columnNo = 3,
            comment = "HsCode"
    )
    @Required
    private String hsCode;
    @DataType(
            type = DataType.Type.BIG_DECIMAL
    )
    @Header(
            name = "hscode.batch.import.header.declare.value",
            columnNo = 4,
            comment = "申报价值"
    )
    @Required
    private String declaredValue;
    @Required
    @Length(
            min = 1,
            max = 3
    )
    @Header(
            name = "hscode.batch.import.header.declare.currency",
            columnNo = 5,
            comment = "币种"
    )
    @In(
            value = {"USD", "HKD", "EUR", "AUD", "CAD", "CNH", "GBP"},
            i18nValue = false
    )
    private String declaredCurrency;
*/

    public UserImportDTO() {
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof UserImportDTO)) {
            return false;
        } else {
            UserImportDTO other = (UserImportDTO)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                return false;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof UserImportDTO;
    }

}
