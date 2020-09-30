
import shared.PresentationUtil._
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object EfCorePatternsDDD {
  import Enumeration._
  
  val chapter1 = chapter(
    auHeadlineSlide(
      <.h2("Scripting / Procedures"),
    ),

    headerSlide("Agenda",
      Enumeration(
        Item.stable("Stored procedures"),
        Item.stable("Trigger"),
        Item.stable("Scripting with SQL Server"),
        Item.stable("Query Notification"),
      ),
    ),
  )

  val chapter2 = chapter(
    headerSlide("Store procedures",
      Enumeration(
        Item.stable("Create Procedure:"),
      ),
      sql("""CREATE PROCEDURE procedure_name
            |   AS
            |  sql_statement""".stripMargin),

      Enumeration(
        Item.stable("To execute procedure:"),
      ),
      sql("EXEC procedure_name;"),
    ),


    headerSlide("SP with parameters",
      Enumeration(
        Item.stable("Create Procedure:"),
      ),
      sql("""CREATE PROCEDURE SelectAllCustomers @City nvarchar(30)
            |  AS
            |  SELECT * FROM Customers WHERE City = @City""".stripMargin),          
      Enumeration(
        Item.stable("To execute procedure:"),
      ),
      sql("EXEC SelectAllCustomers City = 'London'"),
    ),

    headerSlide("SP with parameters",
      Enumeration(
        Item.stable("Create Procedure:"),
      ),
      sql("""CREATE PROCEDURE SelectAllCustomers @City nvarchar(30), @PostalCode nvarchar(10)
            |  AS
            |  SELECT * FROM Customers WHERE City = @City AND PostalCode = @PostalCode""".stripMargin),

      Enumeration(
        Item.stable("To execute procedure:"),
      ),
      sql("EXEC SelectAllCustomers City = 'London', PostalCode = 'WA1 1DP'"),
    ),

    headerSlide("Creating SP in EF Core",
      cSharp("""public partial class spGetStudents : Migration {
              |  protected override void Up(MigrationBuilder migrationBuilder) {
              |      var sp = @"CREATE PROCEDURE [dbo].[GetStudents]
              |                  @FirstName varchar(50)
              |              AS
              |              BEGIN
              |                  SET NOCOUNT ON;
              |                  select * from Students where FirstName like @FirstName +'%'
              |              END";
              |
              |      migrationBuilder.Sql(sp);
              |  }
              |  protected override void Down(MigrationBuilder migrationBuilder) { }
              |}""".stripMargin)
    ),

    headerSlide("image", 
      Enumeration(
        Item.stable("Parameter"),
      ),
    ),

    headerSlideLeftAligned("Calling SP from EF Core (1/2)",
      <.span("Multiple query methods:"), <.br,
      Enumeration(
        Item.stable("Parameter"),
      ),
      cSharp("""var context = new SchoolContext(); 
               |var students = context.Students.FromSql($"GetStudents {name}").ToList();""".stripMargin),
      Enumeration(
        Item.stable("Named Parameters"),
      ),
      cSharp("var students = context.Students.FromSql(\"GetStudents @FirstName\", param).ToList();"),
      Enumeration(
        Item.stable("Numbered Parameters"),
      ),
      cSharp("var students = context.Students.FromSql(\"GetStudents @p0\",\"Bill\").ToList();"),
    ),

    headerSlideLeftAligned("ExecuteSqlCommand()",
      Enumeration(
        Item.stable("When no results is expected")
      ),
      <.br,
      Enumeration(
        Item.stable("Given SP:")
      ),
      sql("""CREATE PROCEDURE CreateStudent @FirstName Varchar(50), @LastName Varchar(50)
            |AS BEGIN
            |  SET NOCOUNT ON;
            |  INSERT INTO Students([FirstName] ,[LastName]) VALUES (@FirstName, @LastName)
            |END""".stripMargin),
      Enumeration(
        Item.stable("When no results is expected")
      ),
      cSharp("""context.Database.ExecuteSqlCommand("CreateStudents @p0, @p1", 
               |    parameters: new[] { "Bill", "Gates" });""".stripMargin),

      <.span("Same with "), <.b("UPDATE"), <.span(" and ") , <.b("DELETE"),
    ),
  )

  val chapter3 = chapter(
    headerSlide("Triggers",
      Enumeration(
        Item.stable("A special kind of SP - run when an event occur"),
        Enumeration(
          Item.stable("DML triggers - runs on INSERT, UPDATE or DELETE statements on table/view"),
          Item.stable("DDL triggers - runs on CREATE, ALTER, DROP"),
          Item.stable("Logon triggers - on logon"),
        ),
        Item.stable("E.g."),
      ),
      sql("""CREATE TRIGGER reminder1
            |  ON Sales.Customer
            |  AFTER INSERT, UPDATE
            |  AS RAISERROR ('Notify Customer Relations', 16, 10);
            |GO""".stripMargin),
    ),

    headerSlide("Trigger example",
      sql("""CREATE TRIGGER Purchasing.LowCredit ON Purchasing.PurchaseOrderHeader  
      |AFTER INSERT  
      |AS  
      |IF (ROWCOUNT_BIG() = 0)
      |RETURN;
      |IF EXISTS (SELECT *  
      |           FROM Purchasing.PurchaseOrderHeader AS p   
      |           JOIN inserted AS i   
      |           ON p.PurchaseOrderID = i.PurchaseOrderID   
      |           JOIN Purchasing.Vendor AS v   
      |           ON v.BusinessEntityID = p.VendorID  
      |           WHERE v.CreditRating = 5)  
      |BEGIN  
      |RAISERROR ('A vendor''s credit rating is too low to accept new purchase orders.', 16, 1);  
      |ROLLBACK TRANSACTION;  
      |RETURN   
      |END;  
      |GO""".stripMargin),
      <.span("From: https://docs.microsoft.com/en-us/sql/t-sql/statements/create-trigger-transact-sql?view=sql-server-2017"),
      notes(
        """This trigger prevents a row from being inserted in the Purchasing.PurchaseOrderHeader
         table when the credit rating of the specified vendor is set to 5 (below average)"""
      )
    ),
  )

  val chapter4 = chapter(
    headerSlide("Accessing Sql Server from Powershell",
      <.span("In PowerShell it could look like this:"), <.br, <.br,

      <.span("$SQLQuery1Output = Invoke-Sqlcmd -query $SQLQuery1 -ServerInstance $SQLInstance -Username $SQLUsername -Password $SQLPassword"), <.br,
      <.span("# Showing count of rows returned"), <.br,
      <.span("$SQLQuery1Output.Count"), <.br,
      <.span("# Selecting first 100 results"), <.br,
      <.span("$SQLQuery1OutputTop100List = $SQLQuery1Output | select -first 100"), <.br,
      <.span("$SQLQuery1OutputTop100List"), <.br,
      <.span("# Selecting customer by ID"), <.br,
      <.span("$SQLQuery1OutputCustomer = $SQLQuery1Output | Where-Object {$_.CustomerID -eq \"100\"}"), <.br,
    ),
  )

  val chapter5 = chapter(
    headerSlide("Query Notifications",
      Enumeration(
        Item.stable("What"),
        Enumeration(
          Item.stable("Allows application to be notified on changes in database"),
          Item.stable("Uses cases: Cache, ..."),
        ),

        Item.stable("Why?"),
        Enumeration(
          Item.stable("Decouple application from database"),
          Item.stable("Data can be changed from multiple places"),
          Item.stable("Avoid polling for changes"),
        ),

        Item.stable("How?"),
        Enumeration(
          Item.stable("Setup notifications on either:"),
          Enumeration(
            Item.stable("SELECT"),
            Item.stable("EXECUTE (must meet requirements of SELECT statement)"),
          ),
        ),
      ),
      <.span("*Requires a Service Broker"),
    )

  )

  val chapterEnd = chapter(
    headerSlide("Exercises",
      <.img(VdomAttr("data-src") := "./img/make-homework-fun.jpg", VdomStyle("height") := "600px"),
    ),

    auHeadlineSlide(
      <.img(VdomAttr("data-src") := "./../../img/ausegl_hvid.png", VdomStyle("max-height") := "600px"),
    ),

    headerSlide(
      "References",
    ),    
  )


  val Talk = ScalaComponent
    .builder[Unit]("Presentation")
    .renderStatic(
      <.div(
        ^.cls := "reveal",
        <.div(
          ^.cls := "slides",
          chapter1,
          chapter2,
          chapter3,
          chapter4,
          chapter5,
          // chapter6,
          // chapter7,
          // chapter8,
          chapterEnd
        )
      )
    )
    .build

  def main(args: Array[String]): Unit = {
    Talk().renderIntoDOM(dom.document.body)
  }
}