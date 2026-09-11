# Optional AWS deployment outline

AWS deployment is intentionally kept optional for the first version of the project. The beginner path is Railway.

For an AWS version, the target architecture is:

```text
Application Load Balancer
          |
      ECS/Fargate
          |
          +---- RDS PostgreSQL + pgvector
          +---- AWS Secrets Manager
          +---- CloudWatch Logs
```

Container build assets are under `deployment/docker/` so the root repository does not trigger Docker-based Railway builds.

A typical AWS workflow is:

1. Create PostgreSQL on RDS and enable the `vector` extension.
2. Create an ECR repository named `ai-customer-support-platform`.
3. Build the application using `deployment/docker/Dockerfile`.
4. Push the image to ECR.
5. Store the OpenAI API key in Secrets Manager.
6. Register `deployment/aws/ecs-task-definition.json` after replacing account, region, image, and database placeholders.
7. Run the task in ECS/Fargate behind an Application Load Balancer.
8. Configure the security groups so ECS can reach RDS on TCP 5432.
9. Set `DEMO_DATA_ENABLED=false` and use a strong random `JWT_SECRET` for production.

The application exposes `/actuator/health` for health checks.
