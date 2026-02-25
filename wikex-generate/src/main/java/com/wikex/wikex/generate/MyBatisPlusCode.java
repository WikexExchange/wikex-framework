package com.wikex.wikex.generate;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/*****
 * @Author: Bobo
 * @Description: Cloud Mall: Code generation example
 ****/
public class MyBatisPlusCode {

    public static void main(String[] args) {
        // Code generator
        AutoGenerator mpg = new AutoGenerator();

        // Global configuration
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/001/src/main/java");
        gc.setAuthor("markchao");
        gc.setOpen(false);  // Whether to open the output directory after generation
        gc.setServiceName("%sService");    // Service interface naming
        mpg.setGlobalConfig(gc);

        // Data source configuration
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://127.0.0.1:3306/na-contract-one?useUnicode=true&useSSL=false&characterEncoding=utf8");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("123456");
        dsc.setTypeConvert(new MySqlTypeConvert(){
            @Override
            public IColumnType processTypeConvert(GlobalConfig config, String fieldType) {
//                System.out.println("Converting type: " + fieldType);
                 if (fieldType.toLowerCase().contains( "datetime" ) ) {
                    return DbColumnType.DATE;
                 }
                return super.processTypeConvert(config, fieldType);
            }
        });
        mpg.setDataSource(dsc);

        // Package configuration
        PackageConfig pc = new PackageConfig();
        pc.setModuleName("swap");
        pc.setParent("com.wikex.wikex");
        mpg.setPackageInfo(pc);

        // Strategy configuration
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        // Specify tables
        strategy.setInclude(new String[] {"trading_times"});
        // Common fields in the parent class
//        strategy.setSuperEntityColumns("id");
        strategy.setControllerMappingHyphenStyle(true);
//        strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);
        mpg.execute();
    }
}
