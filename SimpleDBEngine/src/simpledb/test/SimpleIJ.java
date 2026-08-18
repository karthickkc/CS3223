package simpledb.test;
import java.sql.*;
import java.util.Scanner;
import simpledb.plan.Plan;
import simpledb.plan.Planner;
import simpledb.query.Scan;
import simpledb.server.SimpleDB;
import simpledb.tx.Transaction;
import simpledb.record.Schema;

public class SimpleIJ {
   public static void main(String[] args) {
      Scanner sc = new Scanner(System.in);
      SimpleDB db = new SimpleDB("studentdb");
      Transaction tx = db.newTx();
      Planner planner = db.planner();

      System.out.print("\nSQL> ");
      while (sc.hasNextLine()) {
         String cmd = sc.nextLine().trim();

         if (cmd.startsWith("exit"))
            break;
         else if (cmd.startsWith("select"))
            doQuery(planner, tx, cmd);
         else
            doUpdate(planner, tx, cmd);

         System.out.print("\nSQL> ");
      }

      tx.commit();
      sc.close();
   }

   private static void doQuery(Planner planner, Transaction tx, String cmd) {
      try {
         Plan p = planner.createQueryPlan(cmd, tx);
         Scan s = p.open();

         Schema sch = p.schema();

         // Print column names
         for (String fldname : sch.fields()) {
            System.out.print(fldname + "\t");
         }
         System.out.println();

         // Print records
         while (s.next()) {
            for (String fldname : sch.fields()) {
               if (sch.type(fldname) == Types.INTEGER) {
                  System.out.print(s.getInt(fldname) + "\t");
               }
               else {
                  System.out.print(s.getString(fldname) + "\t");
               }
            }
            System.out.println();
         }

         s.close();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   private static void doUpdate(Planner planner, Transaction tx, String cmd) {
      try {
         int howmany = planner.executeUpdate(cmd, tx);
         System.out.println(howmany + " records processed");
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }
}