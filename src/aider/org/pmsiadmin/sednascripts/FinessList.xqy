for $i in fn:collection("Pmsi")/(*[1])
return fn:document-uri(fn:root($i))&

let $count:=count(distinct-values(fn:collection("Pmsi")/*/*/@Finess))
for $j in $count
return 
<finessechantillon totalcount="{$j}">{
(for $i in distinct-values(fn:collection("Pmsi")/*/*/@Finess)
let $o:=fn:doc("ListFiness", "Pmsi")/listfiness/finess[@num = $i]
return if ($o)
then <finess num="{string($i)}" name="{string($o)}"/>
else <finess num="{string($i)}"/>)[position() = 10 to 20]
}</finessechantillon>

for $i in (1)
return
<finessechantillon
  totalcount="{count(distinct-values(fn:collection("Pmsi")/*/*/@Finess))}"
  first="1"
  last="10">
{
(for $i in distinct-values(fn:collection("Pmsi")/*/*/@Finess)
let $o:=fn:doc("ListFiness", "Pmsi")/listfiness/finess[@num = $i]
return if ($o)
then <finess num="{string($i)}" name="{string($o)}"/>
else <finess num="{string($i)}"/>)[position() = 1 to 10]
}</finessechantillon>