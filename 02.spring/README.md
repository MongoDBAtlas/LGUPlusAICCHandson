<img src="https://companieslogo.com/img/orig/MDB_BIG-ad812c6c.png?t=1648915248" width="50%" title="Github_Logo"/> <br>

# MongoDB Atlas Training for LGU Plus AICC


<br>

Atlas 콘솔에서 sample data 중 sample_training 데이터를 load합니다. 

### Local Atlas 환경 구축
Atlas CLI 및 Docker를 설치하여 로컬에 개발 환경을 구축 합니다.   

설치 관련 내용은 다음 페이지를 참조 바랍니다.  
https://www.mongodb.com/ko-kr/docs/atlas/cli/current/install-atlas-cli/   

Mac OS 기준 homebrew가 설치 된 경우 다음으로 설치 가능 합니다.


```` 
brew install mongodb-atlas
````

설치가 완료된 경우 다음 명령어로 설치를 확인 할 수 있습니다.

```` 
% atlas --version
atlascli version: 1.12.2
git version: 3b5cdae005a38ac699c54017a4919c956741e6a8
Go version: go1.21.3
   os: darwin
   arch: arm64
   compiler: gc
````

Docker를 설치 한 후 Atlas 개발용 컨테이너를 배포 하여 줍니다.   
````
atlas deployments setup --type local

[Default Settings]
Deployment Name   local9599
MongoDB Version   8.0

? How do you want to set up your local Atlas deployment?  [Use arrows to move, type to filter]
> default - With default settings
  custom - With custom settings
  cancel - Cancel setup

````
default 로 선택 하여 줍니다.   

설치가 완료된 후 연결 테스트를 바로 진행 할 수 있습니다. 별도의 연결 테스트가 필요 하지 않음으로 skip을 선택 하여 줍니다.

````
? How would you like to connect to local9599?  [Use arrows to move, type to filter]
  mongosh - MongoDB Shell
  compass - MongoDB Compass
  vscode - MongoDB for VsCode
> skip - Skip Connection
````

이후 docker container정보를 조회 하여 Atlas 클러스터의 포트를 확인 합니다.
````
$ docker ps
CONTAINER ID   IMAGE                             COMMAND                  CREATED         STATUS                   PORTS                                           NAMES
7fae37263bee   mongodb/mongodb-atlas-local:8.0   "/usr/local/bin/runn…"   2 minutes ago   Up 2 minutes (healthy)   127.0.0.1:32771->27017/tcp                      local9599
````

포트번호 32771로 연결 가능한 것을 확인 할 수 있습니다.   
접속 할 수 있는 mongodb 주소는 다음과 같습니다.   

````
mongodb://127.0.0.1:32771
````
mongosh로 연결 테스트를 진행 할 수 있습니다.    


### Spring boot
일반 Java Project로 생성하거나 Springframework 프로젝트를 사용해 프로젝트를 시작할 수 있습니다.     
Spring Framework를 이용하는 경우 Spring Data MongoDB를 선택 할 수 있습니다.

SpringBoot에서 MongoDB를 이용한 개발 방법은 4 가지로 구분이 가능 합니다.

MongoDB Driver Only : Spring Framework를 이용하지 않고 Java driver를 사용하는 방법   
Spring Framework & MongoDB : Spring Framework를 이용하지만 Java driver를 사용하는 방법  
Spring Framework & Mongo Template : Spring Framework 와 Template를 방법    
Spring Framework & Spring Data : Spring Framework 와 Respository(JPA)를 이용한 방법 

이번 개발 테스트는 Repository를 이용한 방법을 진행 합니다.   


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


### Spring boot Project 생성

Spring Framework이 제공하는 Respository를 사용하는 것으로, 기본 CRUD외에 필요한 메서드를 repository interface에 작성하고 implementation에 구현해야 합니다. 


[Spring initializr][0]를 다운받아 프로젝트를 생성합니다.
<img src="/02.spring/images/image03.png" width="90%" height="90%">   


Dependency를 POM에 추가 하여 줍니다.  

```` pom.xml
	<dependency>
	      <groupId>org.mongodb</groupId>
	      <artifactId>mongodb-driver-sync</artifactId>
    </dependency>
````


또한 application.properties 에 Atlas 접근 정보를 추가합니다. 
Atlas CLI로 생성한 클러스터의 접근 주소를 입력 하여 줍니다.  

```` application.properties
spring.data.mongodb.uri=mongodb://127.0.0.1:32771/
spring.data.mongodb.database=handson
````

사용할 데이터는 사용자 정보로 User 정보와 Address정보를 가지는 데이터를 사용 합니다.  
데이터 구조는 다음과 같습니다.  

<img src="/02.spring/images/image04.png" width="90%" height="90%">   

Java Class를 추가 하여 줍니다.   
[User, Address] Class를 추가 하여 줍니다. 
Package 이름을 적절하게 수정 하여 줍니다.  

MongoDB와 연결을 위한 연결정보를 생성하는 MongoConfig 클래스를 추가 하여 줍니다.  
내부에 repository를 위한 연결 설정 및 MongoTemplate를 생성하는 것을 포함 하고 있습니다. 

프로젝트 구조는 다음과 같습니다.   

<img src="/02.spring/images/image05.png" width="90%" height="90%">   

Repository가 제공하지 못하는 (Update) Query를 MongoTemplate를 이용하여 처리 합니다. 이를 위해 CustomUserRepository interface를 생성 하여 줍니다. 
ssn을 이용하여 필터링 하고 주소 정보에 대한 타입으로 수정할 주소를 선택 하여 postalCode(우편번호)를 수정 하여 줍니다.  
where ssn={ssn} and Address.type={type}으로 수정할 데이터를 필터링 합니다.  

````
public interface CustomUserRepository {

	UpdateResult updateAddress(String ssn, String type, String postalCode);

}
````

해당 interface 에 대한 구현 클래스를 생성 하여 줍니다. 이름은 CustomUserRepositoryImpl 로 하여야 합니다(interface이름+Impl).    

````
@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository{

	@Override
	public UpdateResult updateAddress(String ssn, String type, String postalCode) {
		// TODO Auto-generated method stub
		return null;
	}

}

````

구현체에 update Query를 MongoTemplate를 이용하여 구현하여 줍니다.  

````
	@Autowired
    private MongoTemplate mongoTemplate;

	@Override
	public UpdateResult updateAddress(String ssn, String type, String postalCode) {
		Query query = new Query();
		query.addCriteria(Criteria.where("ssn").is(ssn));
		query.addCriteria(Criteria.where("addresses.type").is(type));
		
		Update update = new Update();
		update.set("addresses.$.postcode", postalCode);
		
		UpdateResult rs = mongoTemplate.updateFirst(query, update, User.class);
		
		return rs;
	}
````

사용자의 데이터를 처리할 UserRepository Interface를 생성 합니다.

````
public interface UserRepository extends MongoRepository<User, ObjectId>, CustomUserRepository

````

UserRepository는 CRUD를 제공하는 MongoRepository 및 CustomUserRepository를 상속 합니다.  

ssn으로 검색 및 ssn과 age를 이용한 검색을 추가 하여 작성 합니다. 
where ssn={ssn} 과 where ssn={ssn} and age >{age} 를 추가 합니다.
````
	@Autowired
	public List<User> findByssn(String ssn);
	
	@Query("{'ssn': ?0, 'age': {$gt:?1}}")
	List<User> findUserWithAge(String ssn, int age);
````

클래스 구조는 다음과 같습니다.  


<img src="/02.spring/images/image06.png" width="90%" height="90%">   


테스트는 간단히 AiccRepositoryApplication 클래스에 테스트를 위한 코드를 추가 하여 줍니다.

````
	@Autowired
	private UserRepository userRepository;

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			User user = new User();
			user.setAge(50);
			user.setEmail("gildong.hong@email.com");
			user.setName("Gildong Hong");
			user.setSsn("123-456-7890");
			user.setDateOfBirth("Jan. 1st");
			
			List<String> Hobbies = new ArrayList();
			Hobbies.add("Martial arts");
			
			
			user.setHobbies(Hobbies);
			
			List<Address> Address = new ArrayList();
			
			Address.add(new Address("office","서울시 강남구 삼성동","코엑스 6","06132"));
			Address.add(new Address("home","서울시 강남구 역삼동","역삼 한국 아파트 101동 101호","06320"));
			
			user.setAddresses(Address);
			
			userRepository.deleteAll();
			
			userRepository.insert(user);
						
			List<User> ssnRs = userRepository.findByssn("123-456-7890");
			
			for (int i=0;i<ssnRs.size();i++)
			{
				System.out.println(ssnRs.get(i));
			}
			

			List<User> ageRs = userRepository.findUserWithAge("123-456-7890",40);
			
			for (int i=0;i<ageRs.size();i++)
			{
				System.out.println(ageRs.get(i));
			}

			userRepository.updateAddress("123-456-7890", "office", "0000");
		};
	}
````

애플리케이션을 테스트 진행 합니다.  

결과는 다음과 같습니다.
````
User [id=684da70a407037e25b1387b4, ssn=123-456-7890, name=Gildong Hong, age=50, email=gildong.hong@email.com, hobbies=[Martial arts], addresses=[Address(type=office, address=서울시 강남구 삼성동, detail=코엑스 6, postcode=06132), Address(type=home, address=서울시 강남구 역삼동, detail=역삼 한국 아파트 101동 101호, postcode=06320)], DateOfBirth=Jan. 1st]
User [id=684da70a407037e25b1387b4, ssn=123-456-7890, name=Gildong Hong, age=50, email=gildong.hong@email.com, hobbies=[Martial arts], addresses=[Address(type=office, address=서울시 강남구 삼성동, detail=코엑스 6, postcode=06132), Address(type=home, address=서울시 강남구 역삼동, detail=역삼 한국 아파트 101동 101호, postcode=06320)], DateOfBirth=Jan. 1st]
````


`CommandLineRunner`로 애플리케이션을 실행해보겠습니다. 

```
./mvnw spring-boot:run
```
