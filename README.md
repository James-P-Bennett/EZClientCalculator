# (WIP) EZ Client Calculator

A Java desktop application for analyzing paystubs to calculate qualified monthly income for mortgage lending purposes (USDA, FHA, and Conventional loans).

## Overview

This application helps mortgage loan officers quickly and accurately calculate a borrower's qualified monthly income by:
- Extracting data from paystubs (PDF and image formats)
- Performing income calculations following mortgage lending guidelines
- Providing click-to-copy functionality for easy data entry into loan documents
- Flagging potential documentation issues and income variances

## Features

- **Automated PDF Parsing**: Extract paystub data using Apache PDFBox
- **OCR Support**: Process image-based paystubs with Tesseract OCR
- **Lending Guideline Compliance**: Calculations follow USDA, FHA, and Conventional loan requirements
- **Income Analysis**:
  - Expected monthly income calculations
  - YTD pacing analysis
  - Variance detection and guardrail logic
  - Variable income averaging (overtime, commission, bonus)
- **Professional GUI**: Clean JavaFX interface optimized for financial workflows
- **Click-to-Copy**: Every result field copies to clipboard with a single click
- **Warning System**: Automatic detection of data inconsistencies and missing documentation

## System Requirements

- **Java**: JDK 17 or higher
