
package org.zerorm.core.format.db;

/**
 *
 * @author bvan
 */
public interface DB {
    public enum DBType {
        ORACLE(Oracle.class),
        MYSQL(MySQL.class),
        POSTGRES(Postgres.class);
        
        private Class clazz;
        private DBType(Class clazz){ this.clazz = clazz; }
        
        public DB getInstance(){
            DB db = null;
            try {
                db = (DB) clazz.newInstance();
            } catch(Exception ex) { /* Don't expect an error for this one */ }
            return db;
        }
    }
    public String toTimestamp(java.util.Date date);

}
