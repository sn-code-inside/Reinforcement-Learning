------------------------------------------------------------------------
This is the project README file. Here, you should describe your project.
Tell the reader (someone who does not know anything about this project)
all he/she needs to know. The comments should usually include at least:
------------------------------------------------------------------------

PROJECT TITLE:              Hamstern mit Greenfoot
PURPOSE OF PROJECT:         Integration des Java-Hamster-Modells in Greenfoot
VERSION or DATE:            3.0 (04.05.2009)
HOW TO START THIS PROJECT:  Öffnen und Mauern, Körner und Hamster erzeugen
AUTHORS:                    Dietrich Boles
USER INSTRUCTIONS:

Beim Java-Hamster-Modell handelt es sich um ein spezielles didaktisches Modell, 
das Programmieranfängern einen spielerischen Zugang zu der doch eher technischen 
Welt der Programmierung bietet. Programmieranfänger lernen die grundlegenden 
Programmierkonzepte und den Programmentwurf kennen, indem sie so genannte 
Hamster-Programme entwickeln, mit denen sie virtuelle Hamster durch eine virtuelle 
Landschaft steuern und bestimmte Aufgaben bzw. Probleme lösen lassen.
Weitere Informationen finden sich unter http://www.java-hamster-modell.de

Die Grundidee des Java-Hamster-Modells ist ausgesprochen einfach: Sie als 
Programmierer müssen (virtuelle) Hamster durch eine (virtuelle) Landschaft 
steuern und sie gegebene Aufgaben lösen lassen. 

Territorium 

Die Landschaft, in der die Hamster leben, wird durch eine gekachelte Ebene repräsentiert. 
Auf einzelnen Kacheln können kein, ein oder mehrere Körner liegen. Kacheln, auf denen sich 
Körner befinden, sind in den Landschaftsskizzen entsprechend gekennzeichnet. 
Auf den Kacheln des Hamster-Territoriums können weiterhin auch Mauern stehen, was bedeutet, 
dass diese Kacheln blockiert sind. Die Hamster können sie nicht betreten. Es ist nicht möglich, 
dass sich auf einer Kachel sowohl eine Mauer als auch Körner befinden. Das Territorium ist immer 
vollständig von Mauern umgeben. 
Damit jede Kachel des Hamster-Territoriums eindeutig identifiziert werden kann, ist dem 
Hamster-Territorium ein Koordinatensystem zugeordnet. Jede Kachel ist in einer bestimmten 
Reihe und Spalte platziert. Um Kacheln benennen zu können, bekommt jede Kachel eine r-Koordinate 
(r = Reihe) und eine s-Koordinate (s = Spalte). Die Koordinaten sind natürliche Zahlen und 
beginnen bei 0. Die r-Koordinate wird nach unten pro Kachel um eine Zahl größer und die 
s-Koordinate wächst nach rechts hin. Die Kachel in der linken oberen Ecke hat damit die 
Position (r=0/s=0) -- kurz (0/0). 
Eine Klasse namens Territorium repräsentiert Hamster-Territorien. Die Klasse stellt einige 
nützliche Methoden zur Verfügung, um die Beschaffenheit des aktuellen Territoriums abfragen zu können. 

Hamster 

Jeder Hamster steht auf einer der Kacheln des Hamster-Territoriums. Es ist durchaus 
möglich, dass sich auf einer Kachel mehrere Hamster aufhalten. Diese Kachel darf nicht 
durch eine Mauer blockiert sein, sie kann jedoch Körner enthalten. 
Die Hamster können in vier unterschiedlichen Blickrichtungen (Nord, Süd, West, Ost) auf den 
Kacheln stehen. Je nach Blickrichtung werden die Hamster durch unterschiedliche Symbole repräsentiert. 
Hamster können weiterhin kein, ein oder mehrere Körner im Maul haben. 
Mit Hilfe bestimmter Befehle, die gleich noch genauer erläutert werden, kann ein Programmierer 
die Hamster durch ein gegebenes Hamster-Territorium steuern. Die Hamster können dabei von Kachel 
zu Kachel hüpfen, sie können sich drehen, Körner fressen und Körner wieder ablegen. Sie können 
sich die Hamster quasi als virtuelle Prozessoren vorstellen, die im Gegensatz zu realen Computer-Prozessoren 
(zunächst) keine arithmetischen und logischen Operationen ausführen können, sondern in der Lage sind, 
mit einem kleinen Grundvorrat an Befehlen ein Hamster-Territorium zu „erkunden“. 

Hamster-Befehle 

Die Aufgabe eines Hamster-Programmierers besteht darin, Hamster durch eine Landschaft zu 
steuern, um dadurch gegebene Hamster-Aufgaben zu lösen. Zur Steuerung der Hamster müssen 
diesen Anweisungen in Form von Befehlen gegeben werden. Hamster besitzen dabei die Fähigkeit, 
folgende Befehle (Methoden) zu verstehen und auszuführen: 

- void vor(): Der Hamster hüpft eine Kachel in seiner aktuellen Blickrichtung nach vorne.
- void linksUm(): Der Hamster dreht sich auf der Kachel, auf der er gerade steht, um 90 Grad 
    entgegen dem Uhrzeigersinn. 
- void nimm(): Der Hamster frisst von der Kachel, auf der er sich gerade befindet, genau ein Korn, 
    d.h. anschließend hat der Hamster ein Korn mehr im Maul und auf der Kachel liegt ein Korn 
    weniger als vorher. 
- void gib(): Der Hamster legt auf der Kachel, auf der er sich gerade befindet, genau ein Korn 
    aus seinem Maul ab, d.h. er hat anschließend ein Korn weniger im Maul, und auf der Kachel 
    liegt ein Korn mehr als vorher.

Bei den Befehlen void vor(), void nimm() und void gib() können Probleme auftreten, die sich in 
Laufzeitfehlern (Exceptions) äußern: 
- Ein Hamster bekommt den Befehl void vor() und die Kachel in Blickrichtung vor ihm ist durch 
    eine Mauer blockiert. 
- Ein Hamster bekommt den Befehl void nimm() und auf der Kachel, auf der er sich gerade befindet, 
    liegt kein einziges Korn. 
- Ein Hamster bekommt den Befehl void gib() und er hat kein einziges Korn im Maul. 

Um derartige Laufzeitfehler zu vermeiden, existieren drei spezielle Testbefehle: 
- boolean vornFrei(): Liefert den Wert true, falls sich auf der Kachel in Blickrichtung vor dem Hamster 
    keine Mauer befindet. Ist die Kachel durch eine Mauer blockiert, dann wird der Wert false geliefert.
- boolean maulLeer(): Liefert den Wert false, falls der Hamster ein oder mehrere Körner im Maul hat. 
    Befinden sich keine Körner im Maul des Hamsters, dann wird der Wert true geliefert. 
- boolean kornDa(): Liefert den Wert true, falls auf der Kachel, auf der der Hamster gerade steht, 
    ein oder mehrere Körner liegen. Befindet sich kein Korn auf der Kachel, dann wird der Wert false geliefert. 

Es existieren einige weitere Hamster-Befehle, die jedoch zunächst weniger wichtig sind. Bspw. gibt es Befehle, 
mit denen die aktuellen Zustandswerte (Reihe, Spalte, Blickrichtung, Anzahl an Körnern im Maul) eines 
Hamsters abgefragt werden können.



