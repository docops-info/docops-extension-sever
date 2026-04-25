import os
import re
import json
import argparse
import sys
from pathlib import Path
import frontmatter

def slugify(name):
    name = name.lower()
    name = re.sub(r'[^a-z0-9]+', '-', name)
    return name.strip('-')

def extract_links(content):
    # Matches [[target]] or [[target|label]]
    wikilinks = re.findall(r'\[\[([^\]|]+)(?:\|[^\]]+)?\]\]', content)

    # Matches [Source: target.md] or [Source: target1.md, target2.md]
    source_blocks = re.findall(r'\[Source:\s+([^\]]+)\]', content)
    sources = []
    for block in source_blocks:
        parts = [p.strip() for p in block.split(',')]
        for p in parts:
            if p.endswith('.md'):
                sources.append(p[:-3])
            elif p.endswith('.adoc'):
                sources.append(p[:-5])
            else:
                sources.append(p)

    return wikilinks + sources

def parse_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        post = frontmatter.load(f)

    node_id = slugify(file_path.stem)
    label = post.get('title')

    content = post.content
    if not label:
        # Try to find the first # Heading
        match = re.search(r'^#\s+(.+)$', content, re.MULTILINE)
        if match:
            label = match.group(1).strip()
        else:
            label = file_path.stem

    category = post.get('category', 'uncategorised')
    tags = post.get('tags', [])
    if not isinstance(tags, list):
        tags = [tags]

    # Description: first non-heading paragraph, truncated to 280 chars
    description = ""
    lines = content.split('\n')
    for line in lines:
        line = line.strip()
        if line and not line.startswith('#'):
            description = line
            break

    if len(description) > 280:
        description = description[:277] + "..."

    links = extract_links(content)

    return {
        "id": node_id,
        "label": label,
        "category": category,
        "tags": tags,
        "description": description,
        "content": content,
        "links": links
    }

def main():
    parser = argparse.ArgumentParser(description='Parse wiki markdown files into a graph JSON.')
    parser.add_argument('wiki_dir', help='Path to the markdown wiki directory')
    parser.add_argument('--output', default='graph.json', help='Output file path')
    parser.add_argument('--watch', action='store_true', help='Re-run parser on any file change')
    parser.add_argument('--exclude-category', action='append', help='Exclude nodes with this category (can be used multiple times)')

    args = parser.parse_args()

    wiki_path = Path(args.wiki_dir)
    if not wiki_path.is_dir():
        print(f"Error: {args.wiki_dir} is not a directory", file=sys.stderr)
        sys.exit(1)

    def run_parser():
        nodes = []
        edges = []
        all_node_ids = set()

        file_data = {}
        excluded_categories = set(args.exclude_category) if args.exclude_category else set()

        for file in wiki_path.rglob('*.md'):
            try:
                data = parse_file(file)
                if data['category'] in excluded_categories:
                    continue
                file_data[data['id']] = data
                all_node_ids.add(data['id'])
            except Exception as e:
                print(f"Warning: Failed to parse {file}: {e}", file=sys.stderr)

        unresolved_summary = {}

        for node_id, data in file_data.items():
            valid_links = []
            seen_targets = set()
            for target in data['links']:
                target_slug = slugify(target)
                if target_slug in all_node_ids:
                    if target_slug not in seen_targets and target_slug != node_id:
                        seen_targets.add(target_slug)
                        valid_links.append(target_slug)
                        edges.append({"source": node_id, "target": target_slug})
                else:
                    unresolved_summary.setdefault(node_id, []).append(target)

            nodes.append({
                "id": data['id'],
                "label": data['label'],
                "category": data['category'],
                "tags": data['tags'],
                "description": data['description'],
                "content": data['content']
            })

        if unresolved_summary:
            print("Summary of unresolved links:", file=sys.stderr)
            for node_id, targets in unresolved_summary.items():
                print(f"  {node_id}: {', '.join(targets)}", file=sys.stderr)

        output_data = {
            "nodes": nodes,
            "edges": edges
        }

        with open(args.output, 'w', encoding='utf-8') as f:
            json.dump(output_data, f, indent=2)

        print(f"Graph written to {args.output}")

    run_parser()

    if args.watch:
        try:
            from watchdog.observers import Observer
            from watchdog.events import FileSystemEventHandler
            import time

            class WikiHandler(FileSystemEventHandler):
                def on_any_event(self, event):
                    if event.is_directory:
                        return
                    if event.src_path.endswith('.md'):
                        print(f"Change detected in {event.src_path}, re-parsing...")
                        run_parser()

            event_handler = WikiHandler()
            observer = Observer()
            observer.schedule(event_handler, str(wiki_path), recursive=True)
            observer.start()
            print(f"Watching {wiki_path} for changes... (Press Ctrl+C to stop)")
            try:
                while True:
                    time.sleep(1)
            except KeyboardInterrupt:
                observer.stop()
            observer.join()
        except ImportError:
            print("Error: watchdog not installed. Run 'pip install watchdog' for --watch support.", file=sys.stderr)

if __name__ == "__main__":
    main()
