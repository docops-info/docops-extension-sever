# UML Diagrams

This section provides an overview of UML diagrams and their usage in software development.

## Plantuml

PlantUML is a popular tool for creating UML diagrams using simple text-based descriptions. It supports various diagram types such as class diagrams, sequence diagrams, and activity diagrams.

### Class Diagram

```plantuml
@startuml
class A {
    +int x
    +method1()
}
@enduml
```

### Activity Diagram

```plantuml
@startuml

(*) -> "parsowanie argumentów"
if "katalogi istnieją" then
-->[true] "budowanie"
else
-->[false] "sprawdzanie katalogów"
--> "tworzenie brakujących katalogów"
->"budowanie projektu"
-right-> "kompilacja"
--> [koniec] (*)
endif
@enduml
```

### Sequence Diagram

```plantuml
@startuml
title Dodawanie hosta
actor UI #red
box "Kontroler" #LightBlue
	control Kontroler
	database kontrolerDB
end box
box "Monitor" #DarkSalmon
	control Monitor
	database monitorDB
end box
autonumber
UI -> Kontroler: POST /host
Kontroler -> Kontroler: Walidacja danych
alt dane poprawne
    Kontroler -> kontrolerDB: dodaj do bazy
    Kontroler -> Monitor: POST /host
	Monitor -> Monitor: Walidacja danych
	alt dane poprane
	    Monitor -> monitorDB: Dodaj do bazy
	    Monitor --> Kontroler: status 201
		Kontroler --> UI: status 201
	else dane niepoprawne
	    Monitor -> Kontroler: status 400
		Kontroler --> UI: status 400
	end
else danie niepoprawne
    Kontroler --> UI: status 400
end

@enduml
```
## Mermaid

Mermaid is another tool for creating UML diagrams and flowcharts. It uses a simple syntax and can be integrated with Markdown files. Mermaid supports sequence diagrams, flowcharts, and class diagrams.

### Class Diagram

```mermaid
---
title: Animal example
---
classDiagram
    note "From Duck till Zebra"
    Animal <|-- Duck
    note for Duck "can fly<br>can swim<br>can dive<br>can help in debugging"
    Animal <|-- Fish
    Animal <|-- Zebra
    Animal : +int age
    Animal : +String gender
    Animal: +isMammal()
    Animal: +mate()
    class Duck{
        +String beakColor
        +swim()
        +quack()
    }
    class Fish{
        -int sizeInFeet
        -canEat()
    }
    class Zebra{
        +bool is_wild
        +run()
    }
```

### State Diagram

```mermaid
---
title: Simple sample
---
stateDiagram-v2
    [*] --> Still
    Still --> [*]

    Still --> Moving
    Moving --> Still
    Moving --> Crash
    Crash --> [*]

```

### Sequence Diagram

```mermaid
sequenceDiagram
    participant A
    participant B
    A->>B: Hello
    B->>A: Hi
```


### Mindmap

```mermaid
mindmap
  root((mindmap))
    Origins
      Long history
      ::icon(fa fa-book)
      Popularisation
        British popular psychology author Tony Buzan
    Research
      On effectiveness<br/>and features
      On Automatic creation
        Uses
            Creative techniques
            Strategic planning
            Argument mapping
    Tools
      Pen and paper
      Mermaid

```

### Architecture

```mermaid

architecture-beta
    group api(logos:aws-lambda)[API]

    service db(logos:aws-aurora)[Database] in api
    service disk1(logos:aws-glacier)[Storage] in api
    service disk2(logos:aws-s3)[Storage] in api
    service server(logos:aws-ec2)[Server] in api

    db:L -- R:server
    disk1:T -- B:server
    disk2:T -- B:db

```

### Event Modeling

```mermaid
eventmodeling

tf 01 ui CartUI
tf 02 cmd AddItem
tf 03 evt ItemAdded

rf 04 evt External.InventoryChanged
tf 05 pcr InventoryProcessor
tf 06 cmd ChangeInventory
tf 07 evt Cart.InventoryChanged

```