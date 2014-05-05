
package org.zerorm.core;

import junit.framework.TestCase;
import static org.zerorm.core.Op.$;
import org.zerorm.core.Table.CTE;
import org.zerorm.core.interfaces.Schema;
import org.zerorm.core.model.Table0001;
import org.zerorm.core.model.Table0003;
import org.zerorm.core.primaries.Fn;
import org.zerorm.core.primaries.ValExpr;

/**
 *
 * @author bvan
 */
public class TableTest extends TestCase {

    
    public void testNativeColumns(){
        Table0001 t1 = new Table0001();
        for(Column c: t1.getColumns()){
            System.out.println(c.formatted());
        }

    }
    
    public void testCommonTableExpression(){
        Table0001 t1 = new Table0001();
        
        CTE cte = new CTE("T1_CTE");
        cte.setExpression( t1.selectAllColumns() );
        Select s = cte.selectAllColumns().with( cte );
        System.out.println(s.formatted());
        
        Table0003 t3 = new Table0003();
        cte = new CTE("T3_CTE");
        cte.$( "pk");
        cte.$( "parent");
        cte.$( "name");
        cte.$( "lev");
        
        Select cte_expr = Select.unionAll( 
                t3.selectAllColumns().selection( new Val<Integer>(1).as( "lev") ),
                cte.select()
                        .selection( t3.getColumns() )
                        .selection( new Sql("lev+1") )
                        .join( t3, t3.pk.eq( cte.getColumn( "parent")) )
                );
        System.out.println(cte_expr.formatted());
        cte.setExpression( cte_expr );
        s = cte.selectAllColumns().with( cte );
        System.out.println( s.formatted() );
        
        s = cte.select($("pk")).with( cte );
        System.out.println( s.formatted() );
        
    }
    
    public void testNonRecursiveCTE(){
        
        // Test based off of test in SQLAlchemy
        String expected0001 = 
                "WITH regional_sales (region, total_sales) AS "
                + "( SELECT orders.region, SUM( orders.amount ) total_sales "
                    + "FROM orders GROUP BY orders.region  ), "
                + "top_regions (region) AS "
                + "( SELECT regional_sales.region "
                    + "FROM regional_sales "
                    + "WHERE regional_sales.total_sales > "
                        + "( SELECT ( SUM( regional_sales.total_sales ) / 10 ) "
                        + "FROM regional_sales ) ) "
                + "SELECT orders.region, orders.product, SUM( orders.quantity ) product_units, "
                    + "SUM( orders.amount ) product_sales "
                + "FROM orders "
                + "WHERE orders.region IN ( SELECT top_regions.region FROM top_regions ) "
                + "GROUP BY orders.region, orders.product ";
       
        class Orders extends Table {
            @Schema Column orders;
            @Schema Column region;
            @Schema Column amount;
            @Schema Column product;
            @Schema Column quantity;
            Orders(){super("orders");}
        }
        Orders orders = new Orders();
        
        CTE regional_sales = new CTE("regional_sales"){
            @Schema Column region;
            @Schema Column total_sales;
        };
        
        regional_sales.setExpression(
                orders.select()
                .selection(
                        orders.region,
                        Fn.SUM.of( orders.amount ).as( "total_sales" ) )
                .groupBy( orders.region )
        );
        
        CTE top_regions = new CTE( "top_regions" ){
            @Schema Column region;
        };
        
        Select totalSum = regional_sales.select( 
                new ValExpr( Op.divided( Fn.SUM.of( regional_sales.$( "total_sales" )), 10) ) );
        top_regions.setExpression( regional_sales.select()
                .selection( regional_sales.$( "region" ) )
                .where(  regional_sales.$( "total_sales" ).gt( totalSum ) )
        );
        
        Select s = new Select(
                        orders.region,
                        orders.product,
                        Fn.SUM.of( orders.quantity ).as( "product_units" ),
                        Fn.SUM.of( orders.amount ).as( "product_sales" ) )
                .from( orders )
                .where( orders.region.in( top_regions.select( top_regions.$( "region" ) ) ) )
                .groupBy( orders.region, orders.product )
                .with( regional_sales )
                .with( top_regions );
        
        String actual0001 = s.formatted();
        System.out.println(expected0001);
        System.out.println(actual0001);
        assertEquals("CTE expression invalid", expected0001, actual0001);

    }
    
    
    
}
