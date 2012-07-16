\nac
CREATE COLLECTION "Pmsi"&

CREATE DOCUMENT "PmsiDocIndice" IN COLLECTION "Pmsi"&
UPDATE INSERT <indice>{"1"}</indice> INTO fn:doc("PmsiDocIndice", "Pmsi")&

\commit

// ========== fonctions d'utilisation ==========

let $i:=fn:doc("PmsiDocIndice", "Pmsi")/indice
update
replace $l in $i
with <indice>{$l/text() + 1}</indice>
return $i

update
replace $l in fn:doc("PmsiDocIndice", "Pmsi")/indice
with <indice>{$l/text() + 1}</indice>&

// ========== fonctions de reporting ============

// Recherche des doublons de finess et heure (impossible de déterminer le dernier inséré)
for $i in distinct-values(fn:collection("Pmsi")/(RSF2009 | RSF2012)/content/@insertionTimeStamp)
for $l in distinct-values(fn:collection("Pmsi")/(RSF2009 | RSF2012)/content[@insertionTimeStamp = string($i)]/RsfHeader/@Finess)
let $items:=fn:collection("Pmsi")/(RSF2009 | RSF2012)/content[@insertionTimeStamp = $i]/RsfHeader[@Finess = $l]
order by $l, $i
return <entry finess="{string($l)}" date="{string($i)}" nb="{count($items)}">
{
  for $m in distinct-values($items/../../name())
  let $items2:=fn:collection("Pmsi")/*[name() = $m]/content[@insertionTimeStamp = $i]/RsfHeader[@Finess = $l]
  return <entry type="{$m}" nb="{count($items2)}"/>
}
</entry>&

// Recherche des doublons pour un finess particulier et une heure dinsertion particulière
let $items:=fn:collection("Pmsi")/(RSF2009 | RSF2012)[
  content/@insertionTimeStamp = "2012-07-03T16:24:43.969+02:00"
  and content/RsfHeader/@Finess = "300007119"]
return count($items)&


// Recherche du dernier rsf inséré par mois particulier et par finess version avec [1]
let $items:=fn:collection("Pmsi")/(RSF2009 | RSF2012)/content/RsfHeader
for $l in distinct-values($items/@Finess/string()),
    $y in distinct-values($items/year-from-date(xs:date(@DateFin))),
    $m in distinct-values($items/month-from-date(xs:date(@DateFin)))
order by $y, $m, $l 
return <entry finess="{$l}" monthfin="{$m}" yearfin="{$y}">
{
(for $item in fn:collection("Pmsi")/(RSF2009 | RSF2012)/content[RsfHeader/@Finess = $l and
                 RsfHeader/month-from-date(xs:date(@DateFin)) = $m and
                 RsfHeader/year-from-date(xs:date(@DateFin)) = $y]
order by $item/xs:dateTime(@insertionTimeStamp) descending
return <entry>{$item/@insertionTimeStamp}</entry>)[1]
}
</entry>&

// Etant donné un mois et une année, Recherche du dernier rsf inséré par finess
for $l in distinct-values(fn:collection("Pmsi")/(RSF2009 | RSF2012)/content/RsfHeader/@Finess/string()),
    $y in distinct-values(fn:collection("Pmsi")/(RSF2009 | RSF2012)/content/RsfHeader[@Finess = $l]/year-from-date(@DateFin)),
    $m in distinct-values(fn:collection("Pmsi")/(RSF2009 | RSF2012)/content/RsfHeader[@Finess = $l and year-from-date(@DateFin) = $y]/month-from-date(@DateFin))
  for $items in fn:collection("Pmsi")/(RSF2009 | RSF2012)/content/RsfHeader[@Finess = $l and year-from-date(@DateFin) = $y and month-from-date(@DateFin)]
  order by 
return <l f = "{$l}" y = "{$y}" m = "{$m}" dada = "{$items/../@insertionTimeStamp}"/>

for $y in distinct-values($l/year-from-date(xs:date(@DateFin))),
    $m in distinct-values($items/)
order by $y, $m, $l 
return <entry finess="{$l}" monthfin="{$m}" yearfin="{$y}">
{
(for $item in fn:collection("Pmsi")/(RSF2009 | RSF2012)/content[RsfHeader/@Finess = $l and
                 RsfHeader/month-from-date(xs:date(@DateFin)) = $m and
                 RsfHeader/year-from-date(xs:date(@DateFin)) = $y]
order by $item/xs:dateTime(@insertionTimeStamp) descending
return <entry>{$item/@insertionTimeStamp}</entry>)[1]
}
</entry>&

// Vérification que tous les Finess sont bien les mêmes dans un même rsf
for $i in fn:collection("Pmsi")/(RSF2009 | RSF2012)/content
return <entry type="{$i/../name()}" insertionTimeStamp="{$i/@insertionTimeStamp/string()}">
{
  for $j in $i/RsfHeader
  return <content datefin="{$j/@DateFin}" Finess="{$j/@Finess}">
  {
    for $k in ($j/*[@Finess = $j/@Finess] | $j/*/*[@Finess = $j/@Finess])
    return
    <error>
      <name>{$k/name()}</name>
      <numfacture>{$k/NumFacture/string()}</numfacture>
    </error>
  }
  </content>
}
</entry>

// Vérification que toutes les factures sont bien les mêmes dans un rsfa
for $i in fn:collection("Pmsi")/(RSF2009 | RSF2012)/content
return <entry type="{$i/../name()}" insertionTimeStamp="{$i/@insertionTimeStamp/string()}">
{
  for $j in $i/RsfHeader
  return <content datefin="{$j/@DateFin}" Finess="{$j/@Finess}">
  {
    for $k in $j/RsfA,
        $l in $k/*[@NumFacture != $k/@NumFacture]
    return
    <error>
      <name>{$l/name()}</name>
      <numfacture>{$l/@NumFacture/string()}</numfacture>
      <numparent>{$k/@NumFacture/string()}</numparent>
    </error>
  }
  </content>
}
</entry>

(for $i in fn:collection("Pmsi")/(RSF2009 | RSF2012)/content/RsfHeader[
         @Finess="340780600" and
         year-from-date(xs:date(@DateFin)) = 2012 and
         month-from-date(xs:date(@DateFin)) = 5]
order by $i/../xs:dateTime(@insertionTimeStamp) descending
return
<entry insertion="{$i/../xs:dateTime(@insertionTimeStamp)}">
{
  for $k in $i/RsfA,
      $l in $k/*[@NumFacture != $k/@NumFacture]
  return
  <error>
    <name>{$l/name()}</name>
    <numfacture>{$l/@NumFacture/string()}</numfacture>
    <numparent>{$k/@NumFacture/string()}</numparent>
  </error>
}
</entry>
)[1]

