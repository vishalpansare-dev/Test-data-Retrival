# Test Data Retrieval Tool

A flexible Java tool for retrieving data from any API using RestAssured, with support for parameterized input, authentication, and output to CSV/Excel/JSON.

## Features
- **Configurable via `config.properties`**: Set API endpoint, authentication, input/output files, and fields to extract.
- **Supports**: Basic, OAuth2, Header, and Cookie authentication.
- **Input**: CSV, Excel, or JSON files for parameterization.
- **Output**: CSV, Excel, or JSON files.
- **Field Extraction**: Use JSONPath expressions (Jayway JsonPath) to extract any fields from the API response.
- **Logging**: All major steps are logged.

## Quick Start

1. **Clone and build:**
   ```sh
   git clone <repo-url>
   cd "Test data Retrival"
   gradle build
   ```

2. **Edit `config.properties`:**
   ```properties
   base.url=https://api.example.com/items/{id}
   request.method=GET
   auth.required=yes
   auth.type=oauth
   auth.oauth.token=your_token_here
   request.body.file=body.json
   request.headers.file=headers.properties
   input.file=input.csv
   input.file.format=csv
   output.file=output.csv
   data.retrieval.jsonpath=$..["id"],$..["name"],$..["data.color"]
   ```

3. **Prepare input files:**
   - `input.csv`:
     ```csv
     id,name
     1,Google Pixel 6 Pro
     2,iPhone 15 Pro
     ```
   - `body.json` (optional, for POST/PUT):
     ```json
     {
       "id": "1",
       "name": "Google Pixel 6 Pro"
     }
     ```
   - `headers.properties` (optional):
     ```properties
     Custom-Header=custom-value
     ```

4. **Run:**
   ```sh
   gradle run
   ```

5. **Check output:**
   - `output.csv`, `output.xlsx`, or `output.json` as configured.

## Authentication Examples
- **Basic:**
  ```properties
  auth.type=basic
  auth.username=your_user
  auth.password=your_pass
  ```
- **OAuth2:**
  ```properties
  auth.type=oauth
  auth.oauth.token=your_token_here
  ```
- **Header:**
  ```properties
  auth.type=header
  auth.header.name=Authorization
  auth.header.value=Bearer your_token_here
  ```
- **Cookie:**
  ```properties
  auth.type=cookie
  auth.cookie.name=sessionid
  auth.cookie.value=your_cookie_value
  ```

## Example Test Cases

### 1. GET with CSV input, OAuth2, and JSONPath extraction
- `config.properties`:
  ```properties
  base.url=https://jsonplaceholder.typicode.com/posts/{id}
  request.method=GET
  auth.required=no
  input.file=input.csv
  input.file.format=csv
  output.file=output.csv
  data.retrieval.jsonpath=$.id,$.title
  ```
- `input.csv`:
  ```csv
  id
  1
  2
  ```
- **Expected output:** `output.csv` with columns `$.id` and `$.title` for each row.

### 2. POST with JSON body and custom header
- `config.properties`:
  ```properties
  base.url=https://httpbin.org/post
  request.method=POST
  auth.required=no
  request.body.file=body.json
  request.headers.file=headers.properties
  input.file=
  output.file=output.json
  data.retrieval.jsonpath=$.json.id,$.json.name
  ```
- `body.json`:
  ```json
  { "id": "123", "name": "Test Name" }
  ```
- `headers.properties`:
  ```properties
  X-Test-Header=test-value
  ```
- **Expected output:** `output.json` with extracted fields from the response.

### 3. GET with Cookie authentication
- `config.properties`:
  ```properties
  base.url=https://httpbin.org/cookies
  request.method=GET
  auth.required=yes
  auth.type=cookie
  auth.cookie.name=sessionid
  auth.cookie.value=abc123
  input.file=
  output.file=output.json
  data.retrieval.jsonpath=$.cookies.sessionid
  ```
- **Expected output:** `output.json` with the sessionid value from the response.

## Running Tests

You can add JUnit tests in `src/test/java/com/testdataretrival/`.

**Example JUnit test:**
```java
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
import com.testdataretrival.APIUtils;
import io.restassured.response.Response;
import io.restassured.RestAssured;

public class APIUtilsTest {
    @Test
    public void testExtractFields() {
        String json = "{"id":1,"name":"Test","data":{"color":"red"}}";
        Response response = RestAssured.given().body(json).post("https://httpbin.org/post");
        Map<String, Object> fields = APIUtils.extractFields(response, "$.json.id,$.json.name,$.json.data.color");
        assertEquals("1", String.valueOf(fields.get("$.json.id")));
        assertEquals("Test", String.valueOf(fields.get("$.json.name")));
        assertEquals("red", String.valueOf(fields.get("$.json.data.color")));
    }
}
```

You can run tests with:
```sh
gradle test
```

---

For more advanced usage, see the code and comments in each utility class.
