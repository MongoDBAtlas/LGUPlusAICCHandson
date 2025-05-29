<img src="https://companieslogo.com/img/orig/MDB_BIG-ad812c6c.png?t=1648915248" width="50%" title="Github_Logo"/> <br>

# MongoDB Atlas Training for LGU Plus AICC


<br>

Atlas 콘솔에서 sample data 중 sample_training 데이터를 load합니다. 

### Project
일반 Java Project로 생성하거나 Springframework 프로젝트를 사용해 프로젝트를 시작할 수 있습니다.     
Spring Framework를 이용하는 경우 Spring Data MongoDB를 선택 할 수 있습니다.

SpringBoot에서 MongoDB를 이용한 개발 방법은 4 가지로 구분이 가능 합니다.

MongoDB Driver Only : Spring Framework 와 Spring Data를 이용하지 않는 개발 방법   
Spring Framework & MongoDB : Spring Framework를 이용하지만 Spring Data를 이용하지 않는 방법   
Spring Framework & Mongo Template : Spring Framework 와 Template를 이용한 Spring Data 활용    
Spring Framework & Spring Data : Spring Framework 와 Spring Data를 이용한 방법 

### Driver
MongoDB Driver만을 사용하여 개발 시, 단순한 장점이 있으나 Spring Framewok가 제공 되지 않아 해당 API를 사용하지 못하는 단점이 있습니다.    

IDE에서 java project를 새롭게 생성합니다. 
Dependency를 POM에 추가 하여 줍니다.  

```` pom.xml
	<dependency>
	      <groupId>org.mongodb</groupId>
	      <artifactId>mongodb-driver-sync</artifactId>
    	</dependency>
````

MongoClient를 생성 하여 Connection 한 후 Database, Collection을 선택 한 후 쿼리를 진행 합니다.
MongoDB Java driver를 활용해 간단한 데이터 조회를 진행해보겠습니다.

```
package com.mongodb.demo;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;

public class Read {

    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create(System.getProperty("mongodb.uri"))) {
            MongoDatabase sampleTrainingDB = mongoClient.getDatabase("sample_training");
            MongoCollection<Document> gradesCollection = sampleTrainingDB.getCollection("grades");


            // find a list of documents and use a List object instead of an iterator
            List<Document> studentList = gradesCollection.find(gte("student_id", 100)).limit(2).into(new ArrayList<>());
            System.out.println("1. Student list with an ArrayList:");
            for (Document student : studentList) {
                System.out.println(student.toJson());
            }


            // find a list of documents with sort, skip, limit and projection
            List<Document> docs = gradesCollection.find(and(eq("student_id", 70), lte("class_id", 5)))
                                                  .projection(fields(excludeId(), include("class_id", "student_id")))
                                                  .sort(descending("class_id"))
                                                  .limit(2)
                                                  .into(new ArrayList<>());

            System.out.println("2. Student sorted, skipped, limited and projected:");
            for (Document student : docs) {
                System.out.println(student.toJson());
            }
        }
    }
}

```



### MongoTemplates
Spring Framework이 제공하는 Mongo Templates를 이용하여 개발을 진행할 수 있습니다. 
Repository가 제공하는 기본 CRUD 등을 사용하지 않기 때문에 직접 코드 구현이 필요합니다.

[Spring initializr][0]를 다운받아 프로젝트를 생성합니다.
<img src="/02.spring/images/image02.png" width="90%" height="90%">   

Dependency를 추가 하여 줍니다.

```` pom.xml		
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb</artifactId>
		</dependency>
````

또한 application.properties 에 Atlas 접근 정보를 추가합니다.

```` application.properties
spring.data.mongodb.uri=mongodb+srv://<<username>>:<<password>>@<<atlas address>>.mongodb.net
spring.data.mongodb.database=<<database>>
````

데이터는 model을 생성하고 MongoTemplate 객체를 이용하여 CRUD를 처리합니다. 

Model 디렉토리 내 [GroceryItem.java][1]를 작성합니다. 

`CommandLineRunner`로 애플리케이션을 실행해보겠습니다. 

```
./mvnw spring-boot:run
```


### Repository

Spring Framework이 제공하는 Respository를 사용하는 것으로, 기본 CRUD외에 필요한 메서드를 repository interface에 작성하고 implementation에 구현해야 합니다. 

Repository 디렉토리 내에 [ItemRepository][2]를 생성해 MongoRepository interface를 extend한 ItemRepository interface를 생성합니다. 

```
package com.example.mdbspringboot.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.mdbspringboot.model.GroceryItem;

public interface ItemRepository extends MongoRepository<GroceryItem, String> {
	
	@Query("{name:'?0'}")
	GroceryItem findItemByName(String name);
	
	@Query(value="{category:'?0'}", fields="{'name' : 1, 'quantity' : 1}")
	List<GroceryItem> findAll(String category);
	
	public long count();

}
```
사용할 interface를 생성했으니, MdbSpringBootApplication.java 코드 내에서 이를 활용해보겠습니다.
MdbSpringBootApplication.java 파일 내 아래 코드를 작성합니다.
```
package com.example.mdbspringboot;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.example.mdbspringboot.model.GroceryItem;
import com.example.mdbspringboot.repository.CustomItemRepository;
import com.example.mdbspringboot.repository.ItemRepository;

@SpringBootApplication
@EnableMongoRepositories
public class MdbSpringBootApplication implements CommandLineRunner{
	
	@Autowired
	ItemRepository groceryItemRepo;
	
	@Autowired
	CustomItemRepository customRepo;
	
	List<GroceryItem> itemList = new ArrayList<GroceryItem>();

	public static void main(String[] args) {
		SpringApplication.run(MdbSpringBootApplication.class, args);
	}
	
	public void run(String... args) {
		
		// Clean up any previous data
		groceryItemRepo.deleteAll(); // Doesn't delete the collection
		
		System.out.println("-------------CREATE GROCERY ITEMS-------------------------------\n");
		
		createGroceryItems();
		
		System.out.println("\n----------------SHOW ALL GROCERY ITEMS---------------------------\n");
		
		showAllGroceryItems();
		
		System.out.println("\n-----------UPDATE CATEGORY NAME OF ALL GROCERY ITEMS----------------\n");
		
		updateCategoryName("snacks");
		
		System.out.println("\n-------------------THANK YOU---------------------------");
						
	}
	
	// CRUD operations

	//CREATE
	void createGroceryItems() {
		System.out.println("Data creation started...");

		groceryItemRepo.save(new GroceryItem("Whole Wheat Biscuit", "Whole Wheat Biscuit", 5, "snacks"));
		groceryItemRepo.save(new GroceryItem("Kodo Millet", "XYZ Kodo Millet healthy", 2, "millets"));
		groceryItemRepo.save(new GroceryItem("Dried Red Chilli", "Dried Whole Red Chilli", 2, "spices"));
		groceryItemRepo.save(new GroceryItem("Pearl Millet", "Healthy Pearl Millet", 1, "millets"));
		groceryItemRepo.save(new GroceryItem("Cheese Crackers", "Bonny Cheese Crackers Plain", 6, "snacks"));
		
		System.out.println("Data creation complete...");
	}
	
	// READ
	// 1. Show all the data
	 public void showAllGroceryItems() {
		 
		 itemList = groceryItemRepo.findAll();
		 
		 itemList.forEach(item -> System.out.println(getItemDetails(item)));
	 }
	 
	 // UPDATE APPROACH 1: Using MongoRepository
	 public void updateCategoryName(String category) {
		 
		 // Change to this new value
		 String newCategory = "munchies";
		 
		 // Find all the items with the category 
		 List<GroceryItem> list = groceryItemRepo.findAll(category);
		 
		 list.forEach(item -> {
			 // Update the category in each document
			 item.setCategory(newCategory);
		 });
		 
		 // Save all the items in database
		 List<GroceryItem> itemsUpdated = groceryItemRepo.saveAll(list);
		 
		 if(itemsUpdated != null)
			 System.out.println("Successfully updated " + itemsUpdated.size() + " items.");		 
	 }
	 
	 

}
```


[0]: https://start.spring.io/
[1]: https://github.com/mongodb-developer/mongodb-springboot/blob/main/mdb-spring-boot/src/main/java/com/example/mdbspringboot/model/GroceryItem.java
[2]: https://github.com/mongodb-developer/mongodb-springboot/blob/main/mdb-spring-boot/src/main/java/com/example/mdbspringboot/repository/ItemRepository.java
