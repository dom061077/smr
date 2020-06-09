package com.smr.utils

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream
import org.grails.core.DefaultGrailsDomainClass
import java.text.SimpleDateFormat

class Utils{
    private static def formatValue(def value){
        if(value instanceof java.sql.Date){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")
            return sdf.format(value)
        }
        
        return value
            
    }
    
    private static def getPropertyValue(String propertyName,def obj){
        String[] fields = propertyName.split("\\.")
        if(fields.length==1){
            
            
            //return obj?.getPersistentValue(propertyName);
            return obj?."${propertyName}"
        }
        String nextField = propertyName.substring(propertyName.indexOf(".")+1);
        //return getPropertyValue(nextField,obj?.getPersistentValue(fields[0]))
        return getPropertyValue(nextField,obj?."${fields[0]}")
    }
    
    static String exportarxls(List columns,List propertyNames,List data){
        //String[] columns = ["Name", "Email", "Date Of Birth", "Salary"]
        
       Workbook workbook = new XSSFWorkbook();        
        
       CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Hoja");    
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        //headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);       
        
       for(int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }         
        Font rowFont = workbook.createFont();
        rowFont.setFontHeightInPoints((short) 14);
        CellStyle rowCellStyle = workbook.createCellStyle();
        rowCellStyle.setFont(rowFont)
        
        
        for(int i = 0; i < data.size(); i++){
            int j = i+1
            Row row = sheet.createRow(j)
            for(int k = 0;k < propertyNames.size();k++){
                Cell cell = row.createCell(k)
                //DefaultGrailsDomainClass domainClass = ((DefaultGrailsDomainClass)data[i])
                //cell.setCellValue((data[i]).getPersistentValue(propertyNames[k]).toString());
                def fieldValue=getPropertyValue(propertyNames[k],data[i]);
                fieldValue = formatValue(fieldValue)
                
                cell.setCellValue(fieldValue);
                
                cell.setCellStyle(rowCellStyle);
            }
        }

        
        
        
        ByteArrayOutputStream arrayByte = new ByteArrayOutputStream();
        workbook.write(arrayByte);
        //workbook.write(encodedByte)
        //fileOut.close();
        workbook.close();
        byte[] encodedByte = arrayByte.toByteArray()
        
        
        Base64.Encoder enc = Base64.getEncoder();
        byte[] strenc = enc.encode(encodedByte);
        String encode = new String(strenc, "UTF-8");
        return encode
    }
    
}
