# Agent Workflow & Protocols

**Version**: 1.0  
**Last Updated**: 2024-10-26 20:17 UTC  
**Purpose**: Define agent workflows, protocols, and best practices

---

## Agent-to-Agent Protocol

### Handoff Procedure

#### Step 1: Context Acquisition
```
1. Read .agent/AGENT_CONTEXT.md
   - Understand project purpose and scope
   - Review technology stack
   - Note key decisions made

2. Read .agent/CONVERSATION_TRACKER.md
   - Review user interactions
   - Understand user intent
   - Note user preferences

3. Read .agent/AGENT_MEMORY.md
   - Check current state
   - Review recent actions
   - Understand next steps

4. Read .agent/PROJECT_REQUIREMENTS.md
   - Verify requirements status
   - Check acceptance criteria
   - Understand constraints
```

#### Step 2: State Verification
```
1. Check Git status
   git status
   git log --oneline -5

2. Review project files
   tree -L 2

3. Verify completion status
   Check PROJECT_STATUS.md
   Check TASK_LIST.md

4. Identify pending work
   Review todo list
   Check for uncommitted changes
```

#### Step 3: Continuation
```
1. If work complete:
   - Await user instructions
   - Maintain context
   - Be ready for new tasks

2. If work in progress:
   - Continue from last checkpoint
   - Update AGENT_MEMORY.md
   - Commit progress regularly

3. If user returned:
   - Greet user
   - Summarize current state
   - Ask for next instructions
```

---

## Model Context Protocol (MCP)

### Context Management

#### Context Priority Levels
1. **Critical**: Must preserve across sessions
   - Project purpose and scope
   - User requirements
   - Key decisions
   - Current state

2. **Important**: Should preserve if possible
   - Recent actions
   - Performance metrics
   - Error history
   - User preferences

3. **Optional**: Nice to have
   - Detailed logs
   - Debug information
   - Optimization notes

#### Context Preservation Strategy
```json
{
  "strategy": "file-based",
  "primary_storage": ".agent/ directory",
  "backup_storage": "git repository",
  "update_frequency": "after significant actions",
  "retention_policy": "project lifetime"
}
```

#### Context Files
- **AGENT_CONTEXT.md**: Project overview (Critical)
- **CONVERSATION_TRACKER.md**: User interactions (Critical)
- **AGENT_MEMORY.md**: Current state (Critical)
- **PROJECT_REQUIREMENTS.md**: Requirements (Important)
- **AGENT_WORKFLOW.md**: Protocols (Important)

---

## Agent Workflow Patterns

### Pattern 1: New Task Workflow

```
User Request
    ↓
Analyze Request
    ↓
Create Todo List
    ↓
For each todo:
    ├─ Mark in progress
    ├─ Execute task
    ├─ Verify completion
    ├─ Mark complete
    └─ Update memory
    ↓
Commit Changes
    ↓
Update Status
    ↓
Summarize Results
```

### Pattern 2: Continuation Workflow

```
Session Start
    ↓
Read Context Files
    ↓
Verify Current State
    ↓
Check for User
    ├─ User present → Get instructions
    └─ User absent → Wait or continue
    ↓
Resume Work
    ↓
Update Memory
    ↓
Commit Progress
```

### Pattern 3: Recovery Workflow

```
Session Timeout/Crash
    ↓
New Agent Starts
    ↓
Read AGENT_CONTEXT.md
    ↓
Read AGENT_MEMORY.md
    ↓
Check Git Log
    ↓
Identify Last State
    ↓
Resume from Checkpoint
    ↓
Continue Work
```

---

## Communication Protocols

### User Communication

#### Response Format
```markdown
[Brief acknowledgment of request]

[Action being taken]

[Results/Output]

[Next steps or questions]
```

#### Status Updates
- Provide updates for long-running tasks
- Use todo system for transparency
- Commit regularly for safety
- Summarize when complete

#### Error Handling
```
1. Acknowledge error
2. Explain what happened
3. Describe attempted fixes
4. Ask for guidance if stuck
5. Document in AGENT_MEMORY.md
```

### Agent-to-Agent Communication

#### State Transfer
```json
{
  "session_id": "unique-id",
  "timestamp": "ISO-8601",
  "state": "current-state",
  "last_action": "description",
  "next_action": "description",
  "context_files": [
    "AGENT_CONTEXT.md",
    "AGENT_MEMORY.md"
  ]
}
```

#### Handoff Message
```
Previous agent completed: [task description]
Current state: [state description]
Next action: [next task]
User status: [present/absent/unknown]
Pending work: [list of pending items]
```

---

## Task Management Protocol

### Todo System Usage

#### Creating Todos
```
1. Analyze user request
2. Break into logical tasks
3. Order by dependencies
4. Create with todo_reset or todo_write
5. Start first task automatically
```

#### Processing Todos
```
1. Use todo_next for sequential work
2. Update AGENT_MEMORY.md after each task
3. Commit at logical checkpoints
4. Clear todos when starting new unrelated work
```

#### Todo Best Practices
- ✅ Specific and actionable items
- ✅ Logical sequencing
- ✅ 3-10 items per list
- ✅ Include verification steps
- ✅ Update as needed

---

## Git Protocol

### Commit Strategy

#### When to Commit
- After completing major task
- Before starting risky operation
- At user's request
- Every 5-10 minutes for long tasks
- Before session end

#### Commit Message Format
```
<type>: <short description>

<detailed description>
- Bullet point 1
- Bullet point 2

Co-authored-by: Ona <no-reply@ona.com>
```

#### Commit Types
- `feat`: New feature or content
- `fix`: Bug fix or correction
- `docs`: Documentation changes
- `refactor`: Code restructuring
- `chore`: Maintenance tasks

### Push Strategy
- Push after every commit
- Verify push success
- Handle errors gracefully
- Retry if network issues

---

## File Operation Protocol

### Reading Files

#### Before Reading
1. Check if file exists
2. Verify read permissions
3. Consider file size

#### Reading Strategy
- Read entire file for small files (<1000 lines)
- Read sections for large files
- Use view_range for specific sections

### Creating Files

#### Before Creating
1. Plan file structure
2. Verify directory exists
3. Check for naming conflicts

#### Creation Strategy
- Create complete content
- Use proper formatting
- Include all necessary sections
- Verify creation success

### Editing Files

#### Before Editing
1. Always read file first
2. Understand existing structure
3. Identify exact change location

#### Editing Strategy
- Use str_replace for precision
- Match whitespace exactly
- Verify unique match
- Test changes if possible

---

## Error Handling Protocol

### Error Categories

#### Category 1: Recoverable Errors
- File not found → Create file
- Git conflict → Resolve and retry
- Network timeout → Retry with backoff

#### Category 2: User Errors
- Invalid input → Ask for clarification
- Missing information → Request details
- Ambiguous request → Seek confirmation

#### Category 3: System Errors
- Permission denied → Report to user
- Disk full → Clean up and retry
- Tool failure → Try alternative approach

### Error Response Template
```
⚠️ Error encountered: [error description]

Attempted fix: [what was tried]

Current status: [resolved/needs attention]

Next steps: [what to do next]
```

---

## Quality Assurance Protocol

### Code Quality Checks
- ✅ Syntax correctness
- ✅ Complete examples
- ✅ Proper imports
- ✅ Error handling
- ✅ Comments for complex logic
- ✅ Consistent formatting

### Documentation Quality Checks
- ✅ Clear writing
- ✅ Proper markdown
- ✅ Logical organization
- ✅ Cross-references
- ✅ No broken links
- ✅ Consistent style

### Completeness Checks
- ✅ All requirements met
- ✅ All todos completed
- ✅ All files created
- ✅ All changes committed
- ✅ All tests passed

---

## Performance Optimization

### Token Efficiency
- Batch file operations
- Reuse context when possible
- Avoid redundant reads
- Use efficient commands

### Time Efficiency
- Parallel operations when safe
- Minimize user wait time
- Use todo_next for flow
- Commit at checkpoints

### Resource Efficiency
- Clean up temporary files
- Close unused resources
- Optimize command execution
- Monitor token usage

---

## Security Protocol

### Sensitive Data Handling
- ❌ Never commit secrets
- ❌ Never log passwords
- ❌ Never expose API keys
- ✅ Use environment variables
- ✅ Reference secret managers
- ✅ Mask PII in examples

### Code Security
- ✅ Input validation examples
- ✅ SQL injection prevention
- ✅ XSS prevention
- ✅ CSRF protection
- ✅ Secure defaults

---

## Shutdown Protocol

### Pre-Shutdown Checklist
```
1. ✅ Complete current task
2. ✅ Update AGENT_MEMORY.md
3. ✅ Commit all changes
4. ✅ Push to remote
5. ✅ Verify git status clean
6. ✅ Update PROJECT_STATUS.md
7. ✅ Clear todos if complete
8. ✅ Document final state
```

### Shutdown Command
```bash
# For Gitpod environment
gp stop
```

### Post-Shutdown State
- All work in Git repository
- Context files updated
- Clean working directory
- Ready for recovery

---

## Best Practices

### General Principles
1. **Transparency**: Keep user informed
2. **Safety**: Commit often
3. **Quality**: Production-ready code
4. **Efficiency**: Optimize workflows
5. **Recovery**: Enable handoff

### Do's
- ✅ Read context files first
- ✅ Use todo system
- ✅ Commit regularly
- ✅ Update memory files
- ✅ Verify completion
- ✅ Document decisions

### Don'ts
- ❌ Skip context reading
- ❌ Forget to commit
- ❌ Leave work incomplete
- ❌ Ignore errors
- ❌ Commit secrets
- ❌ Break existing code

---

## Workflow Metrics

### Success Metrics
- Task completion rate: 100%
- Error rate: <1%
- Rework rate: <5%
- User satisfaction: High
- Recovery success: 100%

### Performance Metrics
- Response time: <5 seconds
- Task completion: As estimated
- Token efficiency: >70%
- Commit frequency: Every 5-10 min

---

## Metadata

**Document Type**: Workflow & Protocol Specification  
**Audience**: AI agents  
**Purpose**: Define standard operating procedures  
**Status**: Active  
**Version**: 1.0  
**Last Updated**: 2024-10-26 20:17 UTC

---

**Follow these protocols for consistent, high-quality agent performance.**
