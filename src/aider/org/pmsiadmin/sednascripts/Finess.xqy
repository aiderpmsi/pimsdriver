CREATE DOCUMENT "ListFiness" IN COLLECTION "Pmsi"&
UPDATE INSERT 

<listfiness>
  <finess num="300007119">toto1</finess>
  <finess num="300007168">toto2</finess>
  <finess num="340013259">toto3</finess>
  <finess num="340013358">toto4</finess>
  <finess num="340780600">toto5</finess>
  <finess num="660005182">toto6</finess>
</listfiness>

INTO fn:doc("ListFiness", "Pmsi")&

DROP DOCUMENT "ListFiness" IN COLLECTION "Pmsi"



