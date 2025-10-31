# HTTP Methods & Idempotence - Diagrams Index

This directory contains all the visual diagrams for the HTTP Methods and Idempotence learning module.

## 📊 **Diagram List**

### **01. HTTP Methods Classification**
- **File**: `01-http-methods-classification.mmd`
- **Type**: Flowchart
- **Description**: Visual classification of HTTP methods by safety and idempotency
- **Shows**: GET, HEAD, OPTIONS (Safe & Idempotent), PUT, DELETE (Idempotent Only), POST, PATCH (Neither)

### **02. Idempotence Retry Flow**
- **File**: `02-idempotence-retry-flow.mmd`
- **Type**: Flowchart
- **Description**: Decision flow for handling network failures and retries
- **Shows**: Client request → Network issues → Retry logic → Idempotent vs Non-idempotent handling

### **03. GET Operation Sequence**
- **File**: `03-get-operation-sequence.mmd`
- **Type**: Sequence Diagram
- **Description**: GET request flow showing idempotent and safe behavior
- **Shows**: Client → Server → Database interactions for reading data

### **04. PUT Operation Sequence**
- **File**: `04-put-operation-sequence.mmd`
- **Type**: Sequence Diagram
- **Description**: PUT request flow showing idempotent behavior with side effects
- **Shows**: Client → Server → Database interactions for complete resource replacement

### **05. POST Operation Sequence**
- **File**: `05-post-operation-sequence.mmd`
- **Type**: Sequence Diagram
- **Description**: POST request flow showing non-idempotent behavior
- **Shows**: Client → Server → Database interactions for resource creation

### **06. Decision Tree Flowchart**
- **File**: `06-decision-tree-flowchart.mmd`
- **Type**: Flowchart
- **Description**: Decision tree for choosing the right HTTP method
- **Shows**: Purpose-based method selection with safety classifications

### **07. Banking Examples Matrix**
- **File**: `07-banking-examples-matrix.mmd`
- **Type**: Graph
- **Description**: Real-world banking scenarios mapped to HTTP methods
- **Shows**: Banking operations categorized by safety and idempotency

## 🖼️ **Image Generation**

Each `.mmd` file can be:
1. **Viewed directly** in VS Code with Mermaid extensions
2. **Converted to images** using Mermaid CLI or online tools
3. **Embedded in documentation** as image references

## 🔧 **Usage Instructions**

### **View in VS Code**
1. Install Mermaid Chart extension
2. Open any `.mmd` file
3. Use Command Palette: "Mermaid Chart: Preview Diagram"

### **Generate Images** (Optional)
```bash
# Using Mermaid CLI (if installed)
mmdc -i diagram-file.mmd -o image-file.png
```

### **Online Conversion**
Visit [Mermaid Live Editor](https://mermaid.live) and paste the content from any `.mmd` file.

## 📂 **Directory Structure**
```
diagrams/
├── README.md                           # This file
├── 01-http-methods-classification.mmd  # HTTP methods overview
├── 02-idempotence-retry-flow.mmd      # Retry logic flow
├── 03-get-operation-sequence.mmd      # GET operation flow
├── 04-put-operation-sequence.mmd      # PUT operation flow
├── 05-post-operation-sequence.mmd     # POST operation flow
├── 06-decision-tree-flowchart.mmd     # Method selection guide
├── 07-banking-examples-matrix.mmd     # Banking scenarios
└── images/                            # Generated images (if any)
```

## 🎯 **Learning Benefits**

These diagrams provide:
- **Visual Learning**: Complex concepts made simple through graphics
- **Interview Preparation**: Clear understanding of HTTP method behavior
- **Quick Reference**: Easy-to-understand decision trees and classifications
- **Real-world Context**: Banking examples for practical application

Perfect for mastering HTTP methods and idempotence concepts for technical interviews! 🚀