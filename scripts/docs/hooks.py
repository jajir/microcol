"""Keep repository-relative source links usable in published documentation."""

from pathlib import Path
from urllib.parse import quote, unquote, urlsplit, urlunsplit

from markdown.extensions import Extension
from markdown.treeprocessors import Treeprocessor


def repository_url(href, *, source_file, docs_dir, repo_dir, repo_url, revision, image=False):
    """Map links outside docs to the corresponding GitHub source revision."""
    url = urlsplit(href)
    if url.scheme or url.netloc or not url.path or url.path.startswith("/"):
        return href
    target = (Path(source_file).parent / unquote(url.path)).resolve()
    if target.is_relative_to(Path(docs_dir).resolve()):
        return href
    root = Path(repo_dir).resolve()
    if not target.is_relative_to(root) or not target.exists():
        raise ValueError(f"Invalid repository link in {source_file}: {href} -> {target}")

    path = quote(target.relative_to(root).as_posix(), safe="/")
    ref = quote(revision, safe="")
    if image and target.is_file():
        repo_path = urlsplit(repo_url).path.strip("/")
        base = f"https://raw.githubusercontent.com/{repo_path}/{ref}/{path}"
    else:
        kind = "tree" if target.is_dir() else "blob"
        base = f"{repo_url.rstrip('/')}/{kind}/{ref}/{path}"
    result = urlsplit(base)
    return urlunsplit((result.scheme, result.netloc, result.path, url.query, url.fragment))


class RepositoryLinksProcessor(Treeprocessor):
    def __init__(self, md, extension):
        super().__init__(md)
        self.extension = extension

    def run(self, root):
        for element in root.iter():
            attr = {"a": "href", "img": "src"}.get(element.tag)
            if attr and element.get(attr):
                element.set(attr, repository_url(
                    element.get(attr),
                    source_file=self.extension.source_file,
                    image=element.tag == "img",
                    **self.extension.settings,
                ))
        return root


class RepositoryLinksExtension(Extension):
    def extendMarkdown(self, md):
        # Parsed inline links exist at this point; MkDocs rewrites docs paths at 0.
        md.treeprocessors.register(RepositoryLinksProcessor(md, self), "repo_links", 2)


_extension = None


def on_config(config):
    global _extension
    _extension = RepositoryLinksExtension()
    _extension.settings = dict(
        docs_dir=config["docs_dir"],
        repo_dir=Path(config.config_file_path).resolve().parent,
        repo_url=config["repo_url"],
        revision=config["extra"]["source_revision"],
    )
    config["markdown_extensions"].append(_extension)
    return config


def on_page_markdown(markdown, page, **kwargs):
    _extension.source_file = page.file.abs_src_path
    return markdown
