# GenSpring AI Application

A comprehensive Spring Boot application for AI text generation, image analysis, and image generation using OpenAI APIs. This application provides RESTful endpoints for various AI operations with built-in rate limiting, usage statistics, and conversation management.

## 🚀 Features

### Text Generation
- **AI Text Generation**: Generate text using OpenAI's GPT models
- **Text Summarization**: Create concise summaries of provided text
- **Creative Writing**: Generate creative content with higher temperature settings
- **Text Analysis**: Perform detailed analysis of text including tone, themes, and insights

### Image Processing
- **Image Analysis**: Analyze images using OpenAI's Vision API
- **Image Generation**: Generate images using DALL-E models
- **File Upload Support**: Upload and analyze image files directly
- **Multiple Format Support**: JPEG, PNG, GIF, WebP

### Core Features
- **Rate Limiting**: Configurable request limits per user
- **Usage Statistics**: Track API usage, tokens, and performance metrics
- **Conversation History**: Store and retrieve conversation history
- **Error Handling**: Comprehensive error handling and logging
- **API Documentation**: Swagger/OpenAPI 3.0 documentation
- **Health Monitoring**: Built-in health check endpoints

## 🛠️ Technology Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **H2 Database** (Development)
- **Spring Web**
- **Jackson** (JSON processing)
- **Lombok** (Code generation)
- **OpenAPI 3** (API documentation)
- **JUnit 5** (Testing)
- **Apache HttpClient 5** (HTTP client)

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- OpenAI API Key

## ⚙️ Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Shudhu7/genspring-ai.git
cd genspring-ai
```

### 2. Set Environment Variables
```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

Or create a `.env` file:
```env
OPENAI_API_KEY=your_openai_api_key_here
```

### 3. Build the Application
```bash
mvn clean compile
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## 📚 API Documentation

Once the application is running, access the interactive API documentation at:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/api-docs

## 🔗 API Endpoints

### Text Generation Endpoints

#### Generate Text
```http
POST /api/v1/ai/generate
Content-Type: application/json
X-User-ID: optional-user-id

{
  "prompt": "Write a story about AI",
  "model": "gpt-3.5-turbo",
  "maxTokens": 500,
  "temperature": 0.7
}
```

#### Summarize Text
```http
POST /api/v1/ai/summarize
Content-Type: application/json

{
  "prompt": "Long text to summarize..."
}
```

#### Generate Creative Text
```http
POST /api/v1/ai/creative
Content-Type: application/json

{
  "prompt": "Write a creative poem about the ocean"
}
```

#### Analyze Text
```http
POST /api/v1/ai/analyze
Content-Type: application/json

{
  "prompt": "Text to analyze for tone and themes"
}
```

### Image Processing Endpoints

#### Analyze Image
```http
POST /api/v1/image/analyze
Content-Type: application/json

{
  "imageData": "https://example.com/image.jpg",
  "imageType": "url",
  "prompt": "Describe this image in detail"
}
```

#### Upload and Analyze Image
```http
POST /api/v1/image/analyze/upload
Content-Type: multipart/form-data

file: [image file]
prompt: "Analyze this uploaded image"
```

#### Generate Image
```http
POST /api/v1/image/generate
Content-Type: application/json

{
  "prompt": "A beautiful sunset over mountains",
  "size": "1024x1024",
  "quality": "hd",
  "style": "vivid",
  "n": 1
}
```

### Conversation Management

#### Get User Conversations
```http
GET /api/v1/ai/conversations
X-User-ID: user123
```

#### Get Specific Conversation
```http
GET /api/v1/ai/conversations/{id}
```

### Statistics Endpoints

#### Get User Statistics
```http
GET /api/v1/stats/user?user=user123
```

#### Get Recent Statistics
```http
GET /api/v1/stats/recent?days=7
```

#### Get Statistics Summary
```http
GET /api/v1/stats/summary?days=30
```

### Health Check
```http
GET /api/v1/ai/health
```

## ⚡ Rate Limiting

The application includes built-in rate limiting:
- **Default**: 60 requests per minute per user
- **Configurable** via `application.properties`
- **Headers**: `X-RateLimit-Remaining`, `X-RateLimit-Reset`

Rate limit exceeded response:
```json
{
  "error": "Rate limit exceeded",
  "message": "Too many requests. Please try again later.",
  "resetTime": "2024-01-01T12:30:00"
}
```

## 📊 Usage Statistics

The application tracks:
- **Request counts** per user and day
- **Token usage** for text generation
- **Success/failure rates**
- **Average processing times**
- **Popular models** and usage patterns

## 🔧 Configuration

### Application Properties

Key configuration options in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=jdbc:h2:mem:ingaledb
spring.datasource.username=ingale
spring.datasource.password=ingale

# AI Service Configuration
ai.openai.api-key=${OPENAI_API_KEY}
ai.openai.base-url=https://api.openai.com/v1
ai.openai.model=gpt-3.5-turbo
ai.openai.vision-model=gpt-4-vision-preview
ai.openai.image-model=dall-e-3
ai.max-tokens=10000
ai.request.timeout=30000

# Rate Limiting
rate-limit.requests-per-minute=60
rate-limit.enabled=true

# Image Configuration
image.max-file-size=10485760
image.allowed-types=image/jpeg,image/png,image/gif,image/webp

# Logging
logging.level.com.genspring=DEBUG
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API key (required) | - |
| `SERVER_PORT` | Server port | 8080 |
| `RATE_LIMIT_ENABLED` | Enable rate limiting | true |

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AIControllerTest
```

### Test Coverage
The project includes comprehensive unit tests for:
- **Controllers** (Web layer testing)
- **Services** (Business logic testing)
- **Rate limiting** functionality
- **Error handling** scenarios

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/genspring/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Exception handling
│   │   ├── repository/     # Data repositories
│   │   ├── service/        # Business logic
│   │   └── util/          # Utility classes
│   └── resources/
│       ├── application.properties
│       └── banner.txt
└── test/
    └── java/com/genspring/
        └── controller/     # Controller tests
```

## 🚦 Error Handling

The application provides comprehensive error handling:

### Common Error Responses

#### Validation Error (400)
```json
{
  "error": "Validation failed",
  "message": "Request validation failed",
  "fieldErrors": {
    "prompt": "Prompt cannot be blank"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

#### Rate Limit Exceeded (429)
```json
{
  "error": "Rate limit exceeded",
  "message": "Too many requests. Please try again later.",
  "resetTime": "2024-01-01T12:30:00"
}
```

#### Internal Server Error (500)
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-01T12:00:00"
}
```

## 🔍 Monitoring & Observability

### H2 Console
Access the H2 database console at: http://localhost:8080/api/h2-console
- **JDBC URL**: `jdbc:h2:mem:ingaledb`
- **Username**: `ingale`
- **Password**: `ingale`

### Actuator Endpoints
- **Health**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

### Logging
- **Application logs** at DEBUG level for `com.genspring` package
- **SQL logging** enabled for development
- **Console output** with timestamps and thread information

## 🚀 Deployment

### Production Configuration

For production deployment, consider:

1. **Database**: Replace H2 with PostgreSQL/MySQL
2. **Security**: Add authentication and authorization
3. **Monitoring**: Integrate with monitoring solutions
4. **Load Balancing**: Use multiple instances behind a load balancer
5. **Environment Variables**: Externalize all configuration

### Docker Deployment

Create a `Dockerfile`:
```dockerfile
FROM openjdk:17-jre-slim
COPY target/genspring-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
mvn clean package -DskipTests
docker build -t genspring-ai .
docker run -p 8080:8080 -e OPENAI_API_KEY=your_key genspring-ai
```

## 📝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit your changes: `git commit -am 'Add new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- **Documentation**: Check the Swagger UI for detailed API docs
- **Issues**: Create an issue on GitHub

## 🔄 Changelog

### Version 1.0.0
- Initial release with text generation capabilities
- Image analysis and generation features
- Rate limiting and usage statistics
- Comprehensive API documentation
- Health monitoring and error handling

---

Made with ❤️ by the GenSpring Team
