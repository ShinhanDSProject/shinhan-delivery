#!/usr/bin/env python3
"""
GraphRAG Knowledge Search Engine for shinhan-delivery
========================================================
This script queries the project's Knowledge Graph (nodes & relationships)
to perform multi-hop reasoning search across architecture, design system, and test harness nodes.
"""

import sys
import os
import json

def load_knowledge_graph(file_path: str):
    if not os.path.exists(file_path):
        print(f"Error: Knowledge graph file {file_path} not found.")
        sys.exit(1)
    with open(file_path, "r", encoding="utf-8") as f:
        return json.load(f)

def search_graph(query: str, kg_data: dict):
    print("======================================================")
    print(f"🔍 [GraphRAG Engine] Querying Knowledge Graph for: '{query}'")
    print("======================================================")
    
    matching_nodes = []
    for node in kg_data.get("nodes", []):
        if query.lower() in node["id"].lower() or query.lower() in node["description"].lower() or query.lower() in node["type"].lower():
            matching_nodes.append(node)
            
    if not matching_nodes:
        print("No matching nodes found in Knowledge Graph.")
        return

    for node in matching_nodes:
        print(f"\n📍 Node: {node['id']} ({node['type']})")
        print(f"   Description: {node['description']}")
        
        # Traverse relationships (Multi-hop Edges)
        outgoing_edges = [e for e in kg_data.get("edges", []) if e["source"] == node["id"]]
        incoming_edges = [e for e in kg_data.get("edges", []) if e["target"] == node["id"]]
        
        if outgoing_edges:
            print("   Outgoing Connections (Edges):")
            for edge in outgoing_edges:
                print(f"     ──[{edge['relation']}]──▶ {edge['target']}")
                
        if incoming_edges:
            print("   Incoming Connections (Edges):")
            for edge in incoming_edges:
                print(f"     ◀──[{edge['relation']}]── {edge['source']}")

    print("======================================================")

if __name__ == "__main__":
    script_dir = os.path.dirname(os.path.abspath(__file__))
    kg_path = os.path.join(script_dir, "knowledge_graph.json")
    graph_data = load_knowledge_graph(kg_path)
    
    search_term = sys.argv[1] if len(sys.argv) > 1 else "DesignSystem"
    search_graph(search_term, graph_data)
