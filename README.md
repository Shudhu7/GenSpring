# GenSpring AI Application

A comprehensive Spring Boot application for AI text generation and processing, featuring OpenAI integration, usage analytics, and rate limiting.

## 🚀 Features

- **AI Text Generation**: Generate text using OpenAI's GPT models
- **Multiple AI Operations**:
  - Text Generation
  - Text Summarization
  - Creative Writing
  - Text Analysis
- **Rate Limiting**: Configurable request rate limiting per user
- **Usage Analytics**: Track and analyze API usage patterns
- **Conversation History**: Store and retrieve conversation history
- **RESTful API**: Well-documented REST endpoints
- **Error Handling**: Comprehensive global exception handling
- **Health Monitoring**: Built-in health checks and metrics
- **API Documentation**: Interactive Swagger/OpenAPI documentation

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- OpenAI API Key

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd genspring
```

### 2. Set Environment Variables
Set your OpenAI API key as an environment variable:

```bash
# Linux/Mac
export OPENAI_API_KEY="your-openai-api-key-here"

# Windows
set OPENAI_API_KEY=your-openai-api-key-here
```

### 3. Configure Application Properties
The application uses `application.properties` for configuration. Key settings include:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database (H2 in-memory for development)
spring.datasource.url=jdbc:h2:mem:ingaledb
spring.datasource.username=ingale
spring.datasource.password=ingale

# AI Configuration
ai.openai.model=gpt-3.5-turbo
ai.request.timeout=30000
ai.max-tokens=10000

# Rate Limiting
rate-limit.requests-per-minute=60
rate-limit.enabled=true
```

### 4. Build and Run
```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## 📖 API Documentation

Once the application is running, access the interactive API documentation at:
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/api-docs`

### Core Endpoints

#### AI Generation Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/ai/generate` | Generate AI text |
| POST | `/v1/ai/summarize` | Summarize text |
| POST | `/v1/ai/creative` | Generate creative text |
| POST | `/v1/ai/analyze` | Analyze text |

#### Conversation Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/ai/conversations` | Get user conversations |
| GET | `/v1/ai/conversations/{id}` | Get specific conversation |
| GET | `/v1/ai/health` | Health check |

#### Usage Statistics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/stats/user` | Get user statistics |
| GET | `/v1/stats/recent` | Get recent statistics |
| GET | `/v1/stats/summary` | Get statistics summary |
| GET | `/v1/stats/top-users` | Get top users |

## 💡 Usage Examples

### Basic Text Generation
```bash
curl -X POST http://localhost:8080/api/v1/ai/generate \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "prompt": "Write a short story about a robot learning to paint",
    "maxTokens": 500,
    "temperature": 0.7
  }'
```

### Text Summarization
```bash
curl -X POST http://localhost:8080/api/v1/ai/summarize \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "prompt": "Long text to be summarized..."
  }'
```

### Get User Statistics
```bash
curl -X GET http://localhost:8080/api/v1/stats/user \
  -H "X-User-ID: user123"
```

## 🏗️ Architecture

### Project Structure
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
│   │   └── service/        # Business logic
│   └── resources/
│       └── application.properties
└── test/                   # Unit tests
```

### Key Components

- **AIService**: Core service for AI operations
- **RateLimitService**: Handles request rate limiting
- **UsageStatsService**: Tracks usage analytics
- **GlobalExceptionHandler**: Centralized error handling
- **AIController**: REST API endpoints
- **StatsController**: Analytics endpoints

## 🔧 Configuration

### Rate Limiting
Configure rate limiting in `application.properties`:
```properties
rate-limit.requests-per-minute=60
rate-limit.enabled=true
```

### AI Model Settings
```properties
ai.openai.model=gpt-3.5-turbo
ai.request.timeout=30000
ai.max-tokens=10000
```

### Database Configuration
The application uses H2 in-memory database for development:
```properties
spring.datasource.url=jdbc:h2:mem:ingaledb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Access H2 console at: `http://localhost:8080/api/h2-console`

## 📊 Monitoring & Analytics

### Health Checks
- Endpoint: `GET /v1/ai/health`
- Actuator endpoints: `/actuator/health`, `/actuator/metrics`

### Usage Statistics
The application tracks:
- Request counts per user
- Token usage
- Success/failure rates
- Processing times
- Top users

### Logging
Comprehensive logging is configured with different levels:
- Application logs: `DEBUG` level
- SQL queries: `DEBUG` level
- Hibernate bindings: `TRACE` level

## 🧪 Testing

Run the test suite:
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

The project includes comprehensive unit tests for controllers and services.

## 🚀 Deployment

### Production Configuration
For production deployment:

1. **Use a persistent database** (PostgreSQL, MySQL)
2. **Set environment variables**:
   ```bash
   export OPENAI_API_KEY="your-production-key"
   export SPRING_PROFILES_ACTIVE="production"
   ```
3. **Configure application-production.properties**
4. **Set up monitoring and logging**

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/genspring-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔒 Security Considerations

- API key management through environment variables
- Rate limiting to prevent abuse
- Input validation on all endpoints
- Error handling that doesn't expose sensitive information
- CORS configuration for web clients

## 📝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:
- Documentation: Swagger UI at `/swagger-ui.html`
- Issues: Create an issue in the repository

## 🔄 Version History

- **v1.0.0**: Initial release with core AI features
  - Text generation and processing
  - Rate limiting
  - Usage analytics
  - API documentation

---

**GenSpring AI Application** - Empowering applications with intelligent text processing capabilities.
