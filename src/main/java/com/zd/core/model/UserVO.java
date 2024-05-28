package com.zd.core.model;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.zd.core.excel.annotation.Header;
import com.zd.core.excel.annotation.Length;
import com.zd.core.excel.annotation.Required;

@HeadStyle(fillForegroundColor = 9, rightBorderColor = 22, topBorderColor = 22, leftBorderColor = 22, bottomBorderColor = 22)
@HeadFontStyle(fontHeightInPoints = 16)
@ContentFontStyle(fontHeightInPoints = 12)
@ExcelIgnoreUnannotated
public class UserVO {

    @Header(name = "user.id", columnNo = 1, comment = "id")
    @ExcelProperty
    @ColumnWidth(30)
    @HeadFontStyle(color = 0)
    private String id;

    @Header(name = "user.name", columnNo = 2, comment = "name")
    @ExcelProperty
    @ColumnWidth(30)
    @HeadFontStyle(color = 0)
    private String name;

    @ExcelProperty
    @ColumnWidth(120)
    @HeadFontStyle(color = 10)
    @Header(name = "user.error", columnNo = Integer.MAX_VALUE,comment = "error message")
    private String error;

    public UserVO() {
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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
